/* ==========================================================================
   export.js — exportação em CSV e envio por e-mail
   O CSV sai em formato pt-BR (delimitador ";" e vírgula decimal) para abrir
   direto no Excel — e continua sendo relido por este próprio dashboard.
   ========================================================================== */
(function (global) {
  'use strict';

  var SEP = ';';
  var BOM = '\uFEFF';   // faz o Excel abrir o arquivo como UTF-8

  function cell(v) {
    if (v === null || v === undefined) return '';
    if (typeof v === 'number') {
      if (!isFinite(v)) return '';
      return String(v).replace('.', ',');   // decimal pt-BR
    }
    var s = String(v);
    if (s.indexOf(SEP) >= 0 || s.indexOf('"') >= 0 || /[\r\n]/.test(s)) {
      return '"' + s.replace(/"/g, '""') + '"';
    }
    return s;
  }

  function toCsv(headers, rows) {
    var out = [headers.map(cell).join(SEP)];
    for (var i = 0; i < rows.length; i++) out.push(rows[i].map(cell).join(SEP));
    return BOM + out.join('\r\n');
  }

  function download(filename, text) {
    var blob = new Blob([text], { type: 'text/csv;charset=utf-8;' });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(function () { URL.revokeObjectURL(url); }, 1500);
  }

  function stamp() {
    var d = new Date();
    function p(n) { return String(n).padStart(2, '0'); }
    return d.getFullYear() + p(d.getMonth() + 1) + p(d.getDate()) + '_' + p(d.getHours()) + p(d.getMinutes());
  }

  /* ------------------------------------------- 1) Anúncios filtrados */

  var DETAIL_HEADERS = [
    'cidade', 'moeda', 'id', 'nome', 'anfitriao', 'regiao', 'bairro', 'tipo_acomodacao',
    'latitude', 'longitude', 'preco', 'preco_moeda_comum', 'taxa_cambio',
    'noites_minimas', 'avaliacoes', 'avaliacoes_por_mes', 'ultima_avaliacao',
    'disponibilidade_365', 'anuncios_do_anfitriao', 'outlier'
  ];

  function exportDetail(result, opts) {
    if (!result || !result.cities.length) return 0;
    var rows = [];

    result.cities.forEach(function (city) {
      var ds = city.ds;
      for (var k = 0; k < city.idx.length; k++) {
        var i = city.idx[k];
        var r = ds.rows[i];
        var p = Outliers.effectivePrice(ds, i, opts.outlierAction);
        rows.push([
          ds.cityName, ds.currency, r.id, r.name, r.hostName, r.group, r.neighbourhood, r.roomType,
          ds.lat[i], ds.lon[i],
          p, p === null ? null : p * ds.fxRate, ds.fxRate,
          r.minNights, r.reviews, r.reviewsPerMonth, r.lastReview,
          r.availability, r.hostListings,
          ds.outlierMask[i] ? 'sim' : 'nao'
        ]);
      }
    });

    download('airbnb_anuncios_filtrados_' + stamp() + '.csv', toCsv(DETAIL_HEADERS, rows));
    return rows.length;
  }

  /* ---------------------------------------- 2) Resumo estatístico */

  var SUMMARY_HEADERS = [
    'cidade', 'moeda', 'taxa_cambio', 'anuncios_filtrados', 'anuncios_com_preco', 'sem_preco',
    'preco_medio', 'mediana', 'minimo', 'maximo', 'p25', 'p75', 'iqr', 'p5', 'p95',
    'desvio_padrao', 'coef_variacao', 'moda',
    'outliers_detectados', 'metodo_outlier', 'acao_outlier',
    'bairros_distintos', 'noites_minimas_media', 'disponibilidade_media',
    'ocupacao_estimada', 'avaliacoes_media'
  ];

  function summaryRows(result, opts) {
    return result.cities.map(function (c) {
      var s = c.stats;
      var oi = c.ds.outlierInfo || {};
      return [
        c.ds.cityName, c.ds.currency, c.ds.fxRate,
        c.count, s.count, c.ds.noPriceCount,
        round(s.mean), round(s.median), round(s.min), round(s.max),
        round(s.q1), round(s.q3), round(s.iqr), round(s.p05), round(s.p95),
        round(s.stdDev), round(s.cv, 3), round(s.mode),
        oi.count === undefined ? null : oi.count,
        Outliers.LABELS[opts.outlierMethod] || opts.outlierMethod,
        Outliers.ACTION_LABELS[opts.outlierAction] || opts.outlierAction,
        c.byNeighbourhood.size, round(c.avgMinNights, 1), round(c.avgAvailability, 0),
        round(c.occupancy, 3), round(c.avgReviews, 1)
      ];
    });
  }

  function round(v, d) {
    if (v === null || v === undefined || !isFinite(v)) return null;
    var f = Math.pow(10, d === undefined ? 2 : d);
    return Math.round(v * f) / f;
  }

  function exportSummary(result, opts) {
    if (!result || !result.cities.length) return 0;
    var rows = summaryRows(result, opts);
    download('airbnb_resumo_estatistico_' + stamp() + '.csv', toCsv(SUMMARY_HEADERS, rows));
    return rows.length;
  }

  /* ------------------------------------------------ 3) Texto do resumo */

  function buildSummaryText(result, opts, filterDescription) {
    var L = [];
    L.push('DASHBOARD AIRBNB — VIAJAR É LEGAL');
    L.push('Gerado em ' + new Date().toLocaleString('pt-BR'));
    L.push('');
    L.push('FILTROS APLICADOS');
    L.push(filterDescription || 'nenhum');
    L.push('');
    L.push('TRATAMENTO DE OUTLIERS: ' + (Outliers.LABELS[opts.outlierMethod] || opts.outlierMethod) +
           ' / ' + (Outliers.ACTION_LABELS[opts.outlierAction] || opts.outlierAction));
    L.push('');

    result.cities.forEach(function (c) {
      var s = c.stats;
      var cur = c.ds.currency;
      L.push('=== ' + c.ds.cityName.toUpperCase() + ' (' + (cur || 'moeda não informada') + ') ===');
      L.push('Anúncios no filtro: ' + Stats.fmtNum(c.count) + '  |  com preço: ' + Stats.fmtNum(s.count));
      L.push('Média: ' + Stats.fmtMoney(s.mean, cur) + '  |  Mediana: ' + Stats.fmtMoney(s.median, cur));
      L.push('Mínimo: ' + Stats.fmtMoney(s.min, cur) + '  |  Máximo: ' + Stats.fmtMoney(s.max, cur));
      L.push('Q1: ' + Stats.fmtMoney(s.q1, cur) + '  |  Q3: ' + Stats.fmtMoney(s.q3, cur) +
             '  |  IQR: ' + Stats.fmtMoney(s.iqr, cur));
      L.push('Desvio padrão: ' + Stats.fmtMoney(s.stdDev, cur) + '  |  CV: ' + Stats.fmtNum(s.cv, 2));
      L.push('Outliers detectados: ' + (c.ds.outlierInfo ? Stats.fmtNum(c.ds.outlierInfo.count) : '—'));
      L.push('Bairros: ' + Stats.fmtNum(c.byNeighbourhood.size) +
             '  |  Disponibilidade média: ' + Stats.fmtNum(c.avgAvailability, 0) + ' dias/ano' +
             '  |  Ocupação estimada: ' + Stats.fmtPct(c.occupancy));
      L.push('');
    });

    if (result.cities.length > 1) {
      L.push('COMPARAÇÃO (indicadores independentes de câmbio)');
      result.cities.forEach(function (c) {
        var n = Compare.normalizedIndicators(c);
        L.push('- ' + c.ds.cityName + ': CV ' + Stats.fmtNum(n.cv, 2) +
               ' | IQR/mediana ' + Stats.fmtNum(n.dispersion, 2) +
               ' | P95/mediana ' + Stats.fmtNum(n.tail, 2) +
               ' | ocupação ' + Stats.fmtPct(n.occupancy));
      });
      L.push('');
    }

    L.push('---');
    L.push('Autor: Dante Darelli — dante.darelli@hotmail.com');
    return L.join('\n');
  }

  /* ------------------------------------------------------ 4) E-mail */

  /**
   * Abre o cliente de e-mail com o resumo no corpo e baixa o CSV filtrado
   * para o usuário anexar (o navegador não anexa arquivos via mailto).
   */
  function sendByEmail(result, opts, filterDescription, to) {
    if (!result || !result.cities.length) return null;

    var n = exportDetail(result, opts);
    var text = buildSummaryText(result, opts, filterDescription);
    var cities = result.cities.map(function (c) { return c.ds.cityName; }).join(' × ');

    var subject = 'Dashboard AirBnB — ' + cities + ' — ' + new Date().toLocaleDateString('pt-BR');
    var body = text + '\n\nO arquivo "airbnb_anuncios_filtrados_' + stamp() +
               '.csv" (' + Stats.fmtNum(n) + ' registros) foi baixado nesta máquina — ' +
               'anexe-o a esta mensagem antes de enviar.';

    // Vários clientes de e-mail cortam mailto acima de ~2.000 caracteres.
    // Com muitas cidades carregadas, o corpo é encurtado e o detalhe fica no CSV.
    var MAX = 1900;
    var truncated = encodeURIComponent(body).length > MAX;
    if (truncated) {
      body = text.split('\n').slice(0, 8).join('\n') +
             '\n\n[Resumo completo no arquivo CSV baixado — ' +
             result.cities.length + ' cidade(s), ' + Stats.fmtNum(n) + ' registros.]' +
             '\n\n---\nAutor: Dante Darelli — dante.darelli@hotmail.com';
    }

    var href = 'mailto:' + encodeURIComponent(to || opts.emailTo || '') +
               '?subject=' + encodeURIComponent(subject) +
               '&body=' + encodeURIComponent(body);

    window.location.href = href;
    return { rows: n, subject: subject, truncated: truncated };
  }

  function copySummary(result, opts, filterDescription) {
    var text = buildSummaryText(result, opts, filterDescription);
    if (navigator.clipboard && navigator.clipboard.writeText) {
      return navigator.clipboard.writeText(text).then(function () { return true; });
    }
    return Promise.reject(new Error('Área de transferência indisponível.'));
  }

  global.Exporter = {
    exportDetail: exportDetail,
    exportSummary: exportSummary,
    buildSummaryText: buildSummaryText,
    sendByEmail: sendByEmail,
    copySummary: copySummary,
    toCsv: toCsv,
    download: download
  };
})(window);
