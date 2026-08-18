/* ==========================================================================
   compare.js — painel de comparação entre cidades
   Duas leituras lado a lado:
     (a) valores monetários, opcionalmente convertidos pela taxa informada;
     (b) indicadores adimensionais, que não dependem de câmbio nenhum.
   ========================================================================== */
(function (global) {
  'use strict';

  function esc(s) {
    return String(s === null || s === undefined ? '' : s)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  /* ------------------------------------------------ editor de câmbio */

  function renderFxEditor(el, result, opts) {
    if (!result.cities.length) { el.innerHTML = ''; return; }

    var html = '<div class="chips" style="margin-bottom:10px">' +
      '<label class="check" style="padding:4px 8px">' +
      '<input type="checkbox" id="cmpCommon"' + (opts.compareInCommon ? ' checked' : '') + '>' +
      '<span class="check__text">Converter para moeda comum</span></label>' +
      '<input class="input input--xs" id="cmpCommonCur" style="width:74px" value="' +
      esc(opts.commonCurrency) + '" title="Código da moeda comum" aria-label="Moeda comum">' +
      '</div>';

    html += '<div class="table-wrap"><table class="tbl"><thead><tr>' +
      '<th>Base</th><th>Moeda</th><th>Taxa p/ moeda comum</th><th>Anúncios</th></tr></thead><tbody>';

    result.cities.forEach(function (c) {
      html += '<tr>' +
        '<td><span class="chip__dot" style="display:inline-block;background:' + c.ds.color + '"></span> ' +
        esc(c.ds.cityName) + '</td>' +
        '<td><input class="input input--xs" data-fx-cur="' + c.ds.id + '" style="width:70px;text-align:right" value="' + esc(c.ds.currency) + '"></td>' +
        '<td><input class="input input--xs" data-fx-rate="' + c.ds.id + '" type="number" step="0.0001" min="0.0001" style="width:100px;text-align:right" value="' + c.ds.fxRate + '"></td>' +
        '<td class="num">' + Stats.fmtNum(c.count) + '</td>' +
        '</tr>';
    });

    html += '</tbody></table></div>' +
      '<p class="faint" style="font-size:11.5px;margin-top:8px">' +
      'A taxa multiplica o preço da base para chegar à moeda comum. Deixe 1 para manter o valor bruto.' +
      '</p>';

    el.innerHTML = html;
  }

  /* --------------------------------------------- tabela de métricas */

  function moneyCell(city, valueNative, opts) {
    if (!isFinite(valueNative)) return '—';
    if (opts.compareInCommon) {
      return Stats.fmtMoney(valueNative * city.ds.fxRate, opts.commonCurrency);
    }
    return Stats.fmtMoney(valueNative, city.ds.currency);
  }

  function deltaCell(a, b) {
    if (!isFinite(a) || !isFinite(b) || b === 0) return '<td class="num faint">—</td>';
    var d = (a - b) / Math.abs(b);
    var cls = d >= 0 ? 'delta-pos' : 'delta-neg';
    return '<td class="num ' + cls + '">' + (d >= 0 ? '+' : '') + Stats.fmtPct(d) + '</td>';
  }

  function renderMetricsTable(el, result, opts) {
    var cities = result.cities;
    if (!cities.length) {
      el.innerHTML = '<div class="empty-state"><div class="empty-state__icon">📊</div>' +
                     'Importe ao menos uma base para comparar.</div>';
      return;
    }

    var showDelta = cities.length === 2;

    var money = [
      ['Preço médio',      function (c) { return c.stats.mean; }],
      ['Mediana',          function (c) { return c.stats.median; }],
      ['Mínimo',           function (c) { return c.stats.min; }],
      ['Máximo',           function (c) { return c.stats.max; }],
      ['P25 (Q1)',         function (c) { return c.stats.q1; }],
      ['P75 (Q3)',         function (c) { return c.stats.q3; }],
      ['IQR (Q3 − Q1)',    function (c) { return c.stats.iqr; }],
      ['P5',               function (c) { return c.stats.p05; }],
      ['P95',              function (c) { return c.stats.p95; }],
      ['Desvio padrão',    function (c) { return c.stats.stdDev; }],
      ['Moda (preço mais frequente)', function (c) { return c.stats.mode; }]
    ];

    var plain = [
      ['Anúncios no filtro',    function (c) { return Stats.fmtNum(c.count); },
                                function (c) { return c.count; }],
      ['Anúncios com preço',    function (c) { return Stats.fmtNum(c.stats.count); },
                                function (c) { return c.stats.count; }],
      ['Sem preço informado',   function (c) { return Stats.fmtNum(c.ds.noPriceCount) + ' (' + Stats.fmtPct(c.ds.noPriceCount / c.ds.count) + ')'; },
                                function (c) { return c.ds.noPriceCount / c.ds.count; }],
      ['Outliers detectados',   function (c) { return c.ds.outlierInfo ? Stats.fmtNum(c.ds.outlierInfo.count) + ' (' + Stats.fmtPct(c.ds.outlierInfo.pct || 0) + ')' : '—'; },
                                function (c) { return c.ds.outlierInfo ? c.ds.outlierInfo.pct : NaN; }],
      ['Bairros distintos',     function (c) { return Stats.fmtNum(c.byNeighbourhood.size); },
                                function (c) { return c.byNeighbourhood.size; }],
      ['Noites mínimas (média)', function (c) { return Stats.fmtNum(c.avgMinNights, 1); },
                                function (c) { return c.avgMinNights; }],
      ['Disponibilidade média (dias/ano)', function (c) { return Stats.fmtNum(c.avgAvailability, 0); },
                                function (c) { return c.avgAvailability; }],
      ['Ocupação estimada',     function (c) { return Stats.fmtPct(c.occupancy); },
                                function (c) { return c.occupancy; }],
      ['Avaliações por anúncio (média)', function (c) { return Stats.fmtNum(c.avgReviews, 1); },
                                function (c) { return c.avgReviews; }]
    ];

    var html = '<div class="table-wrap"><table class="tbl"><thead><tr><th>Métrica</th>';
    cities.forEach(function (c) {
      html += '<th><span class="chip__dot" style="display:inline-block;background:' + c.ds.color + '"></span> ' +
              esc(c.ds.cityName) + '</th>';
    });
    if (showDelta) html += '<th>Δ ' + esc(cities[0].ds.cityName) + ' vs ' + esc(cities[1].ds.cityName) + '</th>';
    html += '</tr></thead><tbody>';

    html += '<tr><td colspan="' + (cities.length + 1 + (showDelta ? 1 : 0)) +
            '" class="h-section" style="padding-top:14px">Valores monetários' +
            (opts.compareInCommon ? ' — convertidos para ' + esc(opts.commonCurrency) : ' — moeda de cada base') +
            '</td></tr>';

    money.forEach(function (m) {
      html += '<tr><td class="metric-name">' + m[0] + '</td>';
      cities.forEach(function (c) { html += '<td class="num">' + moneyCell(c, m[1](c), opts) + '</td>'; });
      if (showDelta) {
        var a = m[1](cities[0]) * (opts.compareInCommon ? cities[0].ds.fxRate : 1);
        var b = m[1](cities[1]) * (opts.compareInCommon ? cities[1].ds.fxRate : 1);
        html += opts.compareInCommon ? deltaCell(a, b)
              : '<td class="num faint" title="Moedas diferentes — ative a conversão">n/d</td>';
      }
      html += '</tr>';
    });

    html += '<tr><td colspan="' + (cities.length + 1 + (showDelta ? 1 : 0)) +
            '" class="h-section" style="padding-top:14px">Volume e demanda</td></tr>';

    plain.forEach(function (m) {
      html += '<tr><td class="metric-name">' + m[0] + '</td>';
      cities.forEach(function (c) { html += '<td class="num">' + m[1](c) + '</td>'; });
      if (showDelta) html += deltaCell(m[2](cities[0]), m[2](cities[1]));
      html += '</tr>';
    });

    html += '</tbody></table></div>';
    el.innerHTML = html;
  }

  /* ------------------------------------- indicadores adimensionais */

  /** Métricas que não dependem de câmbio — a comparação mais honesta. */
  function normalizedIndicators(city) {
    var s = city.stats;
    return {
      cv: s.cv,
      dispersion: isFinite(s.median) && s.median ? s.iqr / s.median : NaN,
      tail: isFinite(s.median) && s.median ? s.p95 / s.median : NaN,
      skew: isFinite(s.median) && s.median ? s.mean / s.median : NaN,
      occupancy: city.occupancy,
      perNeighbourhood: city.byNeighbourhood.size ? city.count / city.byNeighbourhood.size : NaN
    };
  }

  var NORM_ROWS = [
    ['cv', 'Coeficiente de variação (σ/μ)', 'Quanto maior, mais heterogêneo o mercado.'],
    ['dispersion', 'Dispersão relativa (IQR/mediana)', 'Largura do miolo do mercado.'],
    ['tail', 'Peso da cauda (P95/mediana)', 'Quanto o topo se distancia do centro.'],
    ['skew', 'Assimetria (média/mediana)', 'Acima de 1 indica puxada por preços altos.'],
    ['occupancy', 'Ocupação estimada', '1 − disponibilidade/365.'],
    ['perNeighbourhood', 'Anúncios por bairro', 'Densidade da oferta.']
  ];

  function renderNormalized(el, result) {
    var cities = result.cities;
    if (!cities.length) { el.innerHTML = ''; return; }

    var vals = cities.map(normalizedIndicators);

    var html = '<div class="table-wrap"><table class="tbl"><thead><tr><th>Indicador</th>';
    cities.forEach(function (c) {
      html += '<th><span class="chip__dot" style="display:inline-block;background:' + c.ds.color + '"></span> ' +
              esc(c.ds.cityName) + '</th>';
    });
    html += '</tr></thead><tbody>';

    NORM_ROWS.forEach(function (r) {
      html += '<tr><td class="metric-name" title="' + esc(r[2]) + '">' + r[1] + '</td>';
      vals.forEach(function (v) {
        var raw = v[r[0]];
        var txt = (r[0] === 'occupancy') ? Stats.fmtPct(raw)
                : (r[0] === 'perNeighbourhood') ? Stats.fmtNum(raw, 0)
                : Stats.fmtNum(raw, 2);
        html += '<td class="num">' + txt + '</td>';
      });
      html += '</tr>';
    });

    html += '</tbody></table></div>' +
      '<p class="faint" style="font-size:11.5px;margin-top:8px">' +
      'Estes indicadores são razões entre grandezas da mesma moeda, então comparam ' +
      'RJ e NY sem depender de câmbio.</p>';

    el.innerHTML = html;
  }

  /* ------------------------------------------------------- orquestra */

  function render(result, opts) {
    var fx = document.getElementById('cmpFx');
    var tbl = document.getElementById('cmpTable');
    var nrm = document.getElementById('cmpNormalized');
    if (fx) renderFxEditor(fx, result, opts);
    if (tbl) renderMetricsTable(tbl, result, opts);
    if (nrm) renderNormalized(nrm, result);
  }

  global.Compare = { render: render, normalizedIndicators: normalizedIndicators, NORM_ROWS: NORM_ROWS };
})(window);
