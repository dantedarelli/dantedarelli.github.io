/* ==========================================================================
   outliers.js — tratamento dos spikes de preço
   Métodos: IQR (Tukey), corte por percentil e z-score modificado (MAD).
   Ações: remover, winsorizar (grampear no limite) ou apenas destacar.
   Os limites são SEMPRE calculados por cidade — moedas e escalas diferem.
   ========================================================================== */
(function (global) {
  'use strict';

  var CONST_MAD = 0.6745; // fator que torna o MAD comparável ao desvio padrão

  /** Limites [low, high] para um vetor de preços válidos, conforme o método. */
  function computeBounds(values, opts) {
    if (!values.length) return { low: -Infinity, high: Infinity };

    var s = Stats.sorted(values);

    if (opts.outlierMethod === 'percentile') {
      return {
        low: Stats.quantileSorted(s, Math.max(0, opts.pLow) / 100),
        high: Stats.quantileSorted(s, Math.min(100, opts.pHigh) / 100)
      };
    }

    if (opts.outlierMethod === 'mad') {
      var med = Stats.quantileSorted(s, 0.5);
      var dev = new Float64Array(s.length);
      for (var i = 0; i < s.length; i++) dev[i] = Math.abs(s[i] - med);
      var mad = Stats.quantile(dev, 0.5);
      if (!mad) return { low: -Infinity, high: Infinity }; // >50% dos valores iguais
      var span = (opts.madZ * mad) / CONST_MAD;
      return { low: med - span, high: med + span };
    }

    // padrão: IQR / Tukey
    var q1 = Stats.quantileSorted(s, 0.25);
    var q3 = Stats.quantileSorted(s, 0.75);
    var iqr = q3 - q1;
    var k = (typeof opts.iqrK === 'number') ? opts.iqrK : 1.5;
    return { low: q1 - k * iqr, high: q3 + k * iqr };
  }

  /**
   * Marca outliers de um dataset e preenche ds.outlierMask e ds.priceAdj.
   * outlierMask[i] = 1 quando o preço i é considerado spike.
   * priceAdj[i]    = preço já winsorizado (idêntico a price quando a ação não é winsorize).
   */
  function apply(ds, opts) {
    var n = ds.count;
    var mask = ds.outlierMask;
    var adj = ds.priceAdj;
    mask.fill(0);
    adj.set(ds.price);

    if (opts.outlierMethod === 'none') {
      ds.outlierInfo = { method: 'none', action: opts.outlierAction, count: 0, groups: [] };
      return ds.outlierInfo;
    }

    // Agrupa por tipo de acomodação quando pedido; senão, um único grupo global.
    var buckets = new Map();
    for (var i = 0; i < n; i++) {
      if (!ds.priceValid[i]) continue;
      var key = opts.perRoomType ? (ds.rows[i].roomType || '—') : '*';
      var b = buckets.get(key);
      if (!b) { b = []; buckets.set(key, b); }
      b.push(i);
    }

    var totalFlagged = 0;
    var groupsInfo = [];

    buckets.forEach(function (idxList, key) {
      var vals = new Float64Array(idxList.length);
      for (var j = 0; j < idxList.length; j++) vals[j] = ds.price[idxList[j]];

      var b = computeBounds(vals, opts);
      var flagged = 0;

      for (var k = 0; k < idxList.length; k++) {
        var idx = idxList[k];
        var v = ds.price[idx];
        if (v < b.low || v > b.high) {
          mask[idx] = 1;
          flagged++;
          if (opts.outlierAction === 'winsorize') {
            adj[idx] = v < b.low ? b.low : b.high;
          }
        }
      }

      totalFlagged += flagged;
      groupsInfo.push({
        key: key,
        low: b.low,
        high: b.high,
        n: idxList.length,
        flagged: flagged
      });
    });

    ds.outlierInfo = {
      method: opts.outlierMethod,
      action: opts.outlierAction,
      count: totalFlagged,
      pct: ds.count ? totalFlagged / ds.count : 0,
      groups: groupsInfo.sort(function (a, b) { return b.flagged - a.flagged; })
    };
    return ds.outlierInfo;
  }

  /** Reaplica o tratamento em todas as bases carregadas. */
  function applyAll(datasets, opts) {
    datasets.forEach(function (ds) { apply(ds, opts); });
  }

  /**
   * O preço que o resto do dashboard deve usar para o registro i:
   *  - 'remove'    → null quando é outlier (o registro sai das análises de preço)
   *  - 'winsorize' → valor grampeado
   *  - 'highlight' → valor original (só muda a cor no mapa)
   */
  function effectivePrice(ds, i, action) {
    if (!ds.priceValid[i]) return null;
    if (action === 'remove' && ds.outlierMask[i]) return null;
    if (action === 'winsorize') return ds.priceAdj[i];
    return ds.price[i];
  }

  var LABELS = {
    iqr: 'IQR (Tukey)',
    percentile: 'Percentil',
    mad: 'Z-score modificado (MAD)',
    none: 'Sem tratamento'
  };
  var ACTION_LABELS = {
    remove: 'Remover',
    winsorize: 'Winsorizar',
    highlight: 'Só destacar'
  };

  global.Outliers = {
    computeBounds: computeBounds,
    apply: apply,
    applyAll: applyAll,
    effectivePrice: effectivePrice,
    LABELS: LABELS,
    ACTION_LABELS: ACTION_LABELS
  };
})(window);
