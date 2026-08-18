/* ==========================================================================
   stats.js — funções estatísticas puras + formatação
   Reaproveitadas por cards, tooltips do mapa, gráficos e comparação.
   ========================================================================== */
(function (global) {
  'use strict';

  /** Copia e ordena numericamente (aceita Array ou TypedArray). */
  function sorted(values) {
    var arr = (values instanceof Float64Array || values instanceof Float32Array)
      ? Float64Array.from(values)
      : Array.prototype.slice.call(values);
    if (arr.sort === Float64Array.prototype.sort) arr.sort();
    else arr.sort(function (a, b) { return a - b; });
    return arr;
  }

  /** Quantil por interpolação linear sobre um array JÁ ordenado. */
  function quantileSorted(s, p) {
    var n = s.length;
    if (!n) return NaN;
    if (n === 1) return s[0];
    var pos = (n - 1) * p;
    var lo = Math.floor(pos);
    var hi = Math.ceil(pos);
    if (lo === hi) return s[lo];
    return s[lo] + (s[hi] - s[lo]) * (pos - lo);
  }

  function quantile(values, p) { return quantileSorted(sorted(values), p); }

  function mean(values) {
    var n = values.length;
    if (!n) return NaN;
    var s = 0;
    for (var i = 0; i < n; i++) s += values[i];
    return s / n;
  }

  function median(values) { return quantile(values, 0.5); }

  function stdDev(values, avg) {
    var n = values.length;
    if (n < 2) return 0;
    var m = (avg === undefined) ? mean(values) : avg;
    var acc = 0;
    for (var i = 0; i < n; i++) { var d = values[i] - m; acc += d * d; }
    return Math.sqrt(acc / (n - 1));
  }

  /** Valor mais frequente (útil em preços "redondos" tipo 100, 150). */
  function mode(values) {
    if (!values.length) return NaN;
    var counts = new Map(), best = NaN, bestN = -1;
    for (var i = 0; i < values.length; i++) {
      var v = values[i];
      var c = (counts.get(v) || 0) + 1;
      counts.set(v, c);
      if (c > bestN) { bestN = c; best = v; }
    }
    return best;
  }

  /**
   * Resumo estatístico completo. Uma única passagem de ordenação.
   * @param {ArrayLike<number>} values
   * @returns {object}
   */
  function describe(values) {
    var s = sorted(values);
    var n = s.length;
    if (!n) {
      return { count: 0, sum: 0, mean: NaN, median: NaN, min: NaN, max: NaN,
               q1: NaN, q3: NaN, iqr: NaN, p05: NaN, p10: NaN, p90: NaN, p95: NaN,
               stdDev: NaN, cv: NaN, mode: NaN, range: NaN };
    }
    var sum = 0;
    for (var i = 0; i < n; i++) sum += s[i];
    var m = sum / n;
    var q1 = quantileSorted(s, 0.25);
    var q3 = quantileSorted(s, 0.75);
    var sd = stdDev(s, m);
    return {
      count: n,
      sum: sum,
      mean: m,
      median: quantileSorted(s, 0.5),
      min: s[0],
      max: s[n - 1],
      q1: q1,
      q3: q3,
      iqr: q3 - q1,
      p05: quantileSorted(s, 0.05),
      p10: quantileSorted(s, 0.10),
      p90: quantileSorted(s, 0.90),
      p95: quantileSorted(s, 0.95),
      stdDev: sd,
      cv: m ? sd / m : NaN,
      mode: mode(s),
      range: s[n - 1] - s[0]
    };
  }

  /** Regra de Freedman–Diaconis para largura de bin do histograma. */
  function histogram(values, maxBins) {
    var s = sorted(values);
    var n = s.length;
    if (!n) return { bins: [], min: 0, max: 0, width: 0 };
    var min = s[0], max = s[n - 1];
    if (min === max) return { bins: [{ x0: min, x1: min, count: n }], min: min, max: max, width: 0 };

    var iqr = quantileSorted(s, 0.75) - quantileSorted(s, 0.25);
    var width = iqr > 0 ? 2 * iqr / Math.cbrt(n) : (max - min) / 30;
    var count = width > 0 ? Math.ceil((max - min) / width) : 30;
    var cap = maxBins || 60;
    if (!isFinite(count) || count < 1) count = 30;
    if (count > cap) count = cap;
    width = (max - min) / count;

    var bins = new Array(count);
    for (var b = 0; b < count; b++) {
      bins[b] = { x0: min + b * width, x1: min + (b + 1) * width, count: 0 };
    }
    for (var i = 0; i < n; i++) {
      var idx = Math.floor((s[i] - min) / width);
      if (idx >= count) idx = count - 1;
      if (idx < 0) idx = 0;
      bins[idx].count++;
    }
    return { bins: bins, min: min, max: max, width: width };
  }

  /** Quebras por quantil, sem repetições — base da escala de cor do coroplético. */
  function quantileBreaks(values, classes) {
    var s = sorted(values);
    if (!s.length) return [];
    var out = [];
    for (var i = 1; i < classes; i++) {
      var v = quantileSorted(s, i / classes);
      if (!out.length || v > out[out.length - 1]) out.push(v);
    }
    return out;
  }

  /* ------------------------- Formatação ------------------------- */

  var SYMBOLS = { BRL: 'R$', USD: 'US$', EUR: '€', GBP: '£', ARS: 'AR$', CLP: 'CL$', MXN: 'MX$', JPY: '¥' };

  function currencySymbol(code) {
    return SYMBOLS[code] || (code ? code + ' ' : '');
  }

  function fmtNum(v, decimals) {
    if (v === null || v === undefined || !isFinite(v)) return '—';
    var d = (decimals === undefined) ? 0 : decimals;
    return v.toLocaleString('pt-BR', { minimumFractionDigits: d, maximumFractionDigits: d });
  }

  function fmtMoney(v, currency, decimals) {
    if (v === null || v === undefined || !isFinite(v)) return '—';
    var d = (decimals === undefined) ? (Math.abs(v) >= 100 ? 0 : 2) : decimals;
    return currencySymbol(currency) + ' ' + fmtNum(v, d);
  }

  /** Compacta valores grandes: 1.234 → 1,2 mil */
  function fmtCompact(v, decimals) {
    if (v === null || v === undefined || !isFinite(v)) return '—';
    var abs = Math.abs(v);
    if (abs >= 1e9) return fmtNum(v / 1e9, decimals === undefined ? 1 : decimals) + ' bi';
    if (abs >= 1e6) return fmtNum(v / 1e6, decimals === undefined ? 1 : decimals) + ' mi';
    if (abs >= 1e4) return fmtNum(v / 1e3, decimals === undefined ? 1 : decimals) + ' mil';
    return fmtNum(v, abs < 10 && !Number.isInteger(v) ? 1 : 0);
  }

  function fmtPct(v, decimals) {
    if (v === null || v === undefined || !isFinite(v)) return '—';
    return fmtNum(v * 100, decimals === undefined ? 1 : decimals) + '%';
  }

  /** Remove acentos e caixa — usado em busca e casamento de colunas. */
  function normalizeText(s) {
    if (s === null || s === undefined) return '';
    return String(s).normalize('NFD').replace(new RegExp('[\\u0300-\\u036f]', 'g'), '').toLowerCase().trim();
  }

  global.Stats = {
    sorted: sorted,
    quantile: quantile,
    quantileSorted: quantileSorted,
    mean: mean,
    median: median,
    stdDev: stdDev,
    mode: mode,
    describe: describe,
    histogram: histogram,
    quantileBreaks: quantileBreaks,
    currencySymbol: currencySymbol,
    fmtNum: fmtNum,
    fmtMoney: fmtMoney,
    fmtCompact: fmtCompact,
    fmtPct: fmtPct,
    normalizeText: normalizeText
  };
})(window);
