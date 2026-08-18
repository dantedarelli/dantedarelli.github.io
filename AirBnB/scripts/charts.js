/* ==========================================================================
   charts.js — gráficos (Chart.js)
   Todos os gráficos são reconstruídos a partir do resultado filtrado.
   ========================================================================== */
(function (global) {
  'use strict';

  var registry = {};
  var CAT = ['#4aa3ff', '#00a699', '#ffb400', '#c77dff', '#ff5a5f', '#34c77b'];

  function cssVar(name, fallback) {
    var v = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    return v || fallback;
  }

  function theme() {
    return {
      grid: cssVar('--line-soft', '#1f2a3d'),
      text: cssVar('--muted', '#93a3bb'),
      strong: cssVar('--txt', '#e8eef7'),
      panel: cssVar('--panel', '#161f2f'),
      line: cssVar('--line', '#26334a')
    };
  }

  function baseOptions(t) {
    return {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 250 },
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: {
          labels: { color: t.text, boxWidth: 11, boxHeight: 11, usePointStyle: true, font: { size: 11 } }
        },
        tooltip: {
          backgroundColor: t.panel,
          borderColor: t.line,
          borderWidth: 1,
          titleColor: t.strong,
          bodyColor: t.text,
          padding: 9
        }
      },
      scales: {
        x: { grid: { color: t.grid, drawBorder: false }, ticks: { color: t.text, font: { size: 10 } } },
        y: { grid: { color: t.grid, drawBorder: false }, ticks: { color: t.text, font: { size: 10 } }, beginAtZero: true }
      }
    };
  }

  function mount(id, config) {
    var canvas = document.getElementById(id);
    if (!canvas) return;
    if (registry[id]) { registry[id].destroy(); delete registry[id]; }
    registry[id] = new Chart(canvas.getContext('2d'), config);
  }

  function moneyTick(cur) {
    return function (v) { return Stats.currencySymbol(cur) + ' ' + Stats.fmtCompact(v); };
  }

  /* ------------------------------------------------ 1) Histograma de preços */

  function histogramChart(result, opts) {
    var t = theme();
    var cur = opts.commonCurrency;
    var h = Stats.histogram(result.commonValues, 34);

    if (!h.bins.length) return mount('chartHistogram', emptyConfig(t, 'Sem preços no filtro atual'));

    var labels = h.bins.map(function (b) { return Stats.fmtCompact(b.x0); });

    // Uma série por cidade, empilhada nos mesmos bins
    var datasets = result.cities.map(function (city) {
      var counts = new Array(h.bins.length).fill(0);
      for (var i = 0; i < city.common.length; i++) {
        var idx = h.width > 0 ? Math.floor((city.common[i] - h.min) / h.width) : 0;
        if (idx >= counts.length) idx = counts.length - 1;
        if (idx < 0) idx = 0;
        counts[idx]++;
      }
      return {
        label: city.ds.cityName,
        data: counts,
        backgroundColor: city.ds.color + 'cc',
        borderColor: city.ds.color,
        borderWidth: 0,
        borderRadius: 2
      };
    });

    var o = baseOptions(t);
    o.scales.x.stacked = true;
    o.scales.y.stacked = true;
    o.scales.x.title = { display: true, text: 'Preço (' + cur + ')', color: t.text, font: { size: 10 } };
    o.plugins.tooltip.callbacks = {
      title: function (items) {
        var b = h.bins[items[0].dataIndex];
        return Stats.fmtMoney(b.x0, cur) + ' – ' + Stats.fmtMoney(b.x1, cur);
      }
    };

    mount('chartHistogram', { type: 'bar', data: { labels: labels, datasets: datasets }, options: o });
  }

  /* ------------------------------------------- 2) Distribuição por tipo */

  function roomTypeChart(result) {
    var t = theme();
    var totals = new Map();
    result.cities.forEach(function (city) {
      city.byRoomType.forEach(function (v, k) { totals.set(k, (totals.get(k) || 0) + v); });
    });

    var labels = Array.from(totals.keys());
    if (!labels.length) return mount('chartRoomType', emptyConfig(t, 'Sem tipo de acomodação nos dados'));

    var data = labels.map(function (k) { return totals.get(k); });
    var o = baseOptions(t);
    delete o.scales;
    o.plugins.legend.position = 'right';
    o.plugins.tooltip.callbacks = {
      label: function (ctx) {
        var sum = data.reduce(function (a, b) { return a + b; }, 0);
        return ' ' + ctx.label + ': ' + Stats.fmtNum(ctx.parsed) + ' (' + Stats.fmtPct(ctx.parsed / sum) + ')';
      }
    };

    mount('chartRoomType', {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: labels.map(function (_, i) { return CAT[i % CAT.length]; }),
          borderColor: cssVar('--panel', '#161f2f'),
          borderWidth: 2
        }]
      },
      options: o
    });
  }

  /* ------------------------------------ 3) Top bairros por preço mediano */

  function topNeighbourhoodsChart(result, opts) {
    var t = theme();
    var rows = [];

    result.cities.forEach(function (city) {
      city.nbPrices.forEach(function (prices, nb) {
        if (prices.length < 5) return; // evita bairro com 1 anúncio liderando o ranking
        rows.push({
          label: nb + (result.cities.length > 1 ? ' · ' + city.ds.cityName : ''),
          value: Stats.median(prices) * city.ds.fxRate,
          n: prices.length,
          color: city.ds.color
        });
      });
    });

    if (!rows.length) return mount('chartNeighbourhoods', emptyConfig(t, 'Sem bairros com 5+ anúncios'));

    rows.sort(function (a, b) { return b.value - a.value; });
    rows = rows.slice(0, 12).reverse();

    var o = baseOptions(t);
    o.indexAxis = 'y';
    o.plugins.legend.display = false;
    o.scales.x.ticks.callback = moneyTick(opts.commonCurrency);
    o.plugins.tooltip.callbacks = {
      label: function (ctx) {
        var r = rows[ctx.dataIndex];
        return ' mediana ' + Stats.fmtMoney(r.value, opts.commonCurrency) + ' · ' + Stats.fmtNum(r.n) + ' anúncios';
      }
    };

    mount('chartNeighbourhoods', {
      type: 'bar',
      data: {
        labels: rows.map(function (r) { return r.label; }),
        datasets: [{
          data: rows.map(function (r) { return r.value; }),
          backgroundColor: rows.map(function (r) { return r.color + 'cc'; }),
          borderRadius: 3
        }]
      },
      options: o
    });
  }

  /* --------------------------------- 4) Preço mediano por tipo e cidade */

  function priceByRoomTypeChart(result, opts) {
    var t = theme();
    var types = [];
    result.cities.forEach(function (city) {
      city.ds.roomTypes.forEach(function (rt) { if (types.indexOf(rt) < 0) types.push(rt); });
    });
    if (!types.length) return mount('chartPriceByType', emptyConfig(t, 'Sem tipo de acomodação nos dados'));

    var datasets = result.cities.map(function (city) {
      var buckets = new Map();
      for (var k = 0; k < city.idx.length; k++) {
        var i = city.idx[k];
        var p = Outliers.effectivePrice(city.ds, i, opts.outlierAction);
        if (p === null) continue;
        var rt = city.ds.rows[i].roomType || '—';
        var a = buckets.get(rt);
        if (!a) { a = []; buckets.set(rt, a); }
        a.push(p * city.ds.fxRate);
      }
      return {
        label: city.ds.cityName,
        data: types.map(function (rt) {
          var a = buckets.get(rt);
          return a && a.length ? Stats.median(a) : null;
        }),
        backgroundColor: city.ds.color + 'cc',
        borderRadius: 3
      };
    });

    var o = baseOptions(t);
    o.scales.y.ticks.callback = moneyTick(opts.commonCurrency);

    mount('chartPriceByType', { type: 'bar', data: { labels: types, datasets: datasets }, options: o });
  }

  /* ------------------------------- 5) Dispersão (quartis) — "boxplot" */

  function spreadChart(result, opts) {
    var t = theme();
    var cities = result.cities.filter(function (c) { return c.statsCommon.count > 0; });
    if (!cities.length) return mount('chartSpread', emptyConfig(t, 'Sem preços no filtro atual'));

    var labels = cities.map(function (c) { return c.ds.cityName; });

    var o = baseOptions(t);
    o.scales.y.ticks.callback = moneyTick(opts.commonCurrency);
    o.plugins.tooltip.callbacks = {
      label: function (ctx) {
        var s = cities[ctx.dataIndex].statsCommon;
        if (ctx.datasetIndex === 0) {
          return ' P5–P95: ' + Stats.fmtMoney(s.p05, opts.commonCurrency) + ' a ' + Stats.fmtMoney(s.p95, opts.commonCurrency);
        }
        if (ctx.datasetIndex === 1) {
          return ' Q1–Q3: ' + Stats.fmtMoney(s.q1, opts.commonCurrency) + ' a ' + Stats.fmtMoney(s.q3, opts.commonCurrency);
        }
        return ' Mediana: ' + Stats.fmtMoney(s.median, opts.commonCurrency);
      }
    };

    mount('chartSpread', {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'P5 – P95',
            data: cities.map(function (c) { return [c.statsCommon.p05, c.statsCommon.p95]; }),
            backgroundColor: cities.map(function (c) { return c.ds.color + '33'; }),
            borderRadius: 4,
            barPercentage: 0.55
          },
          {
            label: 'Q1 – Q3 (IQR)',
            data: cities.map(function (c) { return [c.statsCommon.q1, c.statsCommon.q3]; }),
            backgroundColor: cities.map(function (c) { return c.ds.color + 'cc'; }),
            borderRadius: 4,
            barPercentage: 0.34
          },
          {
            label: 'Mediana',
            type: 'line',
            data: cities.map(function (c) { return c.statsCommon.median; }),
            borderColor: '#ffffff',
            backgroundColor: '#ffffff',
            pointStyle: 'line',
            pointRadius: 11,
            pointBorderWidth: 3,
            showLine: false
          }
        ]
      },
      options: o
    });
  }

  /* ---------------------------------- 6) Anúncios por ano da avaliação */

  function reviewsTimelineChart(result) {
    var t = theme();
    var years = new Map();

    result.cities.forEach(function (city) {
      var per = new Map();
      for (var k = 0; k < city.idx.length; k++) {
        var lr = city.ds.rows[city.idx[k]].lastReview;
        if (!lr || lr.length < 4) continue;
        var y = parseInt(lr.slice(0, 4), 10);
        if (!isFinite(y) || y < 2008 || y > 2100) continue;
        per.set(y, (per.get(y) || 0) + 1);
        years.set(y, true);
      }
      city._byYear = per;
    });

    var labels = Array.from(years.keys()).sort(function (a, b) { return a - b; });
    if (!labels.length) return mount('chartTimeline', emptyConfig(t, 'Sem datas de avaliação nos dados'));

    var datasets = result.cities.map(function (city) {
      return {
        label: city.ds.cityName,
        data: labels.map(function (y) { return city._byYear.get(y) || 0; }),
        borderColor: city.ds.color,
        backgroundColor: city.ds.color + '22',
        fill: true,
        tension: 0.32,
        pointRadius: 2.5,
        borderWidth: 2
      };
    });

    var o = baseOptions(t);
    o.scales.x.title = { display: true, text: 'Ano da última avaliação', color: t.text, font: { size: 10 } };

    mount('chartTimeline', { type: 'line', data: { labels: labels, datasets: datasets }, options: o });
  }

  function emptyConfig(t, msg) {
    return {
      type: 'bar',
      data: { labels: [msg], datasets: [{ data: [0], backgroundColor: 'transparent' }] },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { enabled: false } },
        scales: {
          x: { ticks: { color: t.text, font: { size: 12 } }, grid: { display: false } },
          y: { display: false }
        }
      }
    };
  }

  function renderAll(result, opts) {
    if (!result) return;
    histogramChart(result, opts);
    roomTypeChart(result);
    topNeighbourhoodsChart(result, opts);
    priceByRoomTypeChart(result, opts);
    spreadChart(result, opts);
    reviewsTimelineChart(result);
  }

  function destroyAll() {
    Object.keys(registry).forEach(function (k) { registry[k].destroy(); delete registry[k]; });
  }

  global.Charts = { renderAll: renderAll, destroyAll: destroyAll };
})(window);
