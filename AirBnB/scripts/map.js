/* ==========================================================================
   map.js — Leaflet: coroplético por hexbin, mapa de calor e pontos
   Não há GeoJSON de bairros na origem dos dados, então os polígonos do
   coroplético são gerados a partir das próprias coordenadas importadas.
   ========================================================================== */
(function (global) {
  'use strict';

  /* Escala sequencial de 7 classes (espelha --c1..--c7 do CSS) */
  var RAMP = ['#2b4b8f', '#2f7fb0', '#40a9a0', '#8fc46a', '#f2c14a', '#f08a3c', '#e0453f'];
  var CAT_COLORS = ['#4aa3ff', '#00a699', '#ffb400', '#c77dff', '#ff5a5f', '#34c77b'];

  var map = null;
  var canvasRenderer = null;
  var layers = { hex: null, heat: null, points: null };
  var legendCtl = null;
  var lastBreaks = null;
  var lastMetricLabel = '';
  var hasFitted = false;
  var pointsShown = 0;

  var THIRD_PI = Math.PI / 3;
  var HEX_ANGLES = [0, THIRD_PI, 2 * THIRD_PI, 3 * THIRD_PI, 4 * THIRD_PI, 5 * THIRD_PI];

  /* ---------------------------------------------------------------- init */

  function init(elId) {
    map = L.map(elId, {
      preferCanvas: true,
      zoomControl: true,
      worldCopyJump: true
    }).setView([0, 0], 2);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap &copy; CARTO'
    }).addTo(map);

    canvasRenderer = L.canvas({ padding: 0.35 });

    legendCtl = L.control({ position: 'bottomright' });
    legendCtl.onAdd = function () {
      var div = L.DomUtil.create('div', 'legend');
      L.DomEvent.disableClickPropagation(div);
      div.innerHTML = '';
      this._div = div;
      return div;
    };
    legendCtl.addTo(map);

    return map;
  }

  function getMap() { return map; }

  /* ------------------------------------------------------------- hexbin */

  /**
   * Agrupa pontos em hexágonos no plano projetado (EPSG:3857).
   * Implementa o mesmo esquema do d3-hexbin: dx = r·√3, dy = r·1,5.
   */
  function hexbin(points, radiusMeters) {
    var dx = radiusMeters * 2 * Math.sin(THIRD_PI);
    var dy = radiusMeters * 1.5;
    var bins = new Map();

    for (var k = 0; k < points.length; k++) {
      var p = points[k];
      var py = p.y / dy, pj = Math.round(py);
      var px = p.x / dx - (pj & 1 ? 0.5 : 0), pi = Math.round(px);
      var py1 = py - pj;

      if (Math.abs(py1) * 3 > 1) {
        var px1 = px - pi;
        var pi2 = pi + (px < pi ? -1 : 1) / 2;
        var pj2 = pj + (py < pj ? -1 : 1);
        var px2 = px - pi2;
        var py2 = py - pj2;
        if (px1 * px1 + py1 * py1 > px2 * px2 + py2 * py2) {
          pi = pi2 + (pj & 1 ? 1 : -1) / 2;
          pj = pj2;
        }
      }

      var key = pi + ':' + pj;
      var b = bins.get(key);
      if (!b) {
        b = {
          x: (pi + (pj & 1 ? 0.5 : 0)) * dx,
          y: pj * dy,
          prices: [],
          avail: [],
          reviews: [],
          n: 0
        };
        bins.set(key, b);
      }
      b.n++;
      if (p.price !== null) b.prices.push(p.price);
      if (p.avail !== null) b.avail.push(p.avail);
      if (p.reviews !== null) b.reviews.push(p.reviews);
    }
    return Array.from(bins.values());
  }

  function metricValue(bin, metric) {
    switch (metric) {
      case 'count':        return bin.n;
      case 'mean':         return bin.prices.length ? Stats.mean(bin.prices) : NaN;
      case 'availability': return bin.avail.length ? Stats.mean(bin.avail) : NaN;
      case 'reviews':      return bin.reviews.length ? Stats.mean(bin.reviews) : NaN;
      default:             return bin.prices.length ? Stats.median(bin.prices) : NaN;
    }
  }

  var METRIC_LABELS = {
    median: 'Preço mediano',
    mean: 'Preço médio',
    count: 'Nº de anúncios',
    availability: 'Disponibilidade (dias/ano)',
    reviews: 'Avaliações (média)'
  };

  /** Índice da classe de cor conforme a escala escolhida. */
  function colorIndex(v, breaks, scale, min, max) {
    if (scale === 'quantile') {
      var i = 0;
      while (i < breaks.length && v > breaks[i]) i++;
      return i;
    }
    var t;
    if (scale === 'log') {
      var a = Math.log(Math.max(1e-6, min));
      var b = Math.log(Math.max(1e-6, max));
      t = (b > a) ? (Math.log(Math.max(1e-6, v)) - a) / (b - a) : 0;
    } else {
      t = (max > min) ? (v - min) / (max - min) : 0;
    }
    return Math.min(RAMP.length - 1, Math.max(0, Math.floor(t * RAMP.length)));
  }

  function hexPolygonLatLngs(cx, cy, r) {
    var pts = [];
    for (var i = 0; i < 6; i++) {
      var a = HEX_ANGLES[i];
      var p = L.point(cx + Math.sin(a) * r, cy - Math.cos(a) * r);
      pts.push(L.CRS.EPSG3857.unproject(p));
    }
    return pts;
  }

  /* --------------------------------------------------------- construção */

  /**
   * Moeda e fator do coroplético: com uma única base, os valores ficam na
   * moeda nativa; com várias, tudo é convertido pela taxa informada na UI,
   * senão USD e BRL cairiam na mesma escala de cor.
   */
  function moneyBasis(result, opts) {
    if (result.cities.length === 1) {
      return { currency: result.cities[0].ds.currency, convert: false };
    }
    return { currency: opts.commonCurrency, convert: true };
  }

  /**
   * Extrai os pontos filtrados de uma cidade, já projetados.
   */
  function projectCity(city, action, convert) {
    var ds = city.ds;
    var factor = convert ? ds.fxRate : 1;
    var out = new Array(city.idx.length);
    for (var k = 0; k < city.idx.length; k++) {
      var i = city.idx[k];
      var ll = L.latLng(ds.lat[i], ds.lon[i]);
      var pt = L.CRS.EPSG3857.project(ll);
      var row = ds.rows[i];
      var pe = Outliers.effectivePrice(ds, i, action);
      out[k] = {
        i: i,
        x: pt.x,
        y: pt.y,
        lat: ds.lat[i],
        lon: ds.lon[i],
        price: pe === null ? null : pe * factor,
        avail: row.availability,
        reviews: row.reviews,
        outlier: ds.outlierMask[i] === 1
      };
    }
    return out;
  }

  function clearLayers() {
    ['hex', 'heat', 'points'].forEach(function (k) {
      if (layers[k]) { map.removeLayer(layers[k]); layers[k] = null; }
    });
  }

  function buildChoropleth(result, opts) {
    var group = L.layerGroup();
    var allValues = [];
    var perCity = [];
    var basis = moneyBasis(result, opts);

    result.cities.forEach(function (city) {
      var pts = projectCity(city, opts.outlierAction, basis.convert);
      if (!pts.length) return;

      // O metro do EPSG:3857 encolhe com o cosseno da latitude — compensa
      // para que o hexágono tenha o tamanho pedido no terreno.
      var latC = (city.ds.bounds.minLat + city.ds.bounds.maxLat) / 2;
      var r = (opts.hexRadiusKm * 1000) / Math.max(0.15, Math.cos(latC * Math.PI / 180));

      var bins = hexbin(pts, r);
      var vals = [];
      bins.forEach(function (b) {
        b.value = metricValue(b, opts.hexMetric);
        if (isFinite(b.value)) vals.push(b.value);
      });
      perCity.push({ city: city, bins: bins, r: r, values: vals });
      allValues = allValues.concat(vals);
    });

    if (!allValues.length) return { layer: group, breaks: [], min: 0, max: 0 };

    var min = Math.min.apply(null, allValues);
    var max = Math.max.apply(null, allValues);
    var breaks = Stats.quantileBreaks(allValues, RAMP.length);

    perCity.forEach(function (pc) {
      var currency = basis.currency;
      var isMoney = (opts.hexMetric === 'median' || opts.hexMetric === 'mean');

      pc.bins.forEach(function (b) {
        if (!isFinite(b.value)) return;
        var ci = colorIndex(b.value, breaks, opts.colorScale, min, max);
        var poly = L.polygon(hexPolygonLatLngs(b.x, b.y, pc.r), {
          renderer: canvasRenderer,
          color: '#0b111c',
          weight: 0.5,
          opacity: 0.55,
          fillColor: RAMP[ci],
          fillOpacity: 0.72
        });

        var pstats = b.prices.length ? Stats.describe(b.prices) : null;
        poly.bindTooltip(
          '<div class="hex-tooltip"><b>' + escapeHtml(pc.city.ds.cityName) + '</b><br>' +
          METRIC_LABELS[opts.hexMetric] + ': <b>' +
          (isMoney ? Stats.fmtMoney(b.value, currency) : Stats.fmtNum(b.value, opts.hexMetric === 'count' ? 0 : 1)) +
          '</b><br>' + Stats.fmtNum(b.n) + ' anúncios' +
          (pstats ? '<br>mediana ' + Stats.fmtMoney(pstats.median, currency) +
                    ' · min ' + Stats.fmtMoney(pstats.min, currency) +
                    ' · máx ' + Stats.fmtMoney(pstats.max, currency) : '') +
          '</div>',
          { sticky: true }
        );
        group.addLayer(poly);
      });
    });

    return { layer: group, breaks: breaks, min: min, max: max };
  }

  function buildHeat(result, opts) {
    var pts = [];
    var maxP = 0;

    result.cities.forEach(function (city) {
      for (var k = 0; k < city.idx.length; k++) {
        var i = city.idx[k];
        var p = Outliers.effectivePrice(city.ds, i, opts.outlierAction);
        var w = 1;
        if (opts.heatByPrice && p !== null) {
          w = p * city.ds.fxRate;
          if (w > maxP) maxP = w;
        }
        pts.push([city.ds.lat[i], city.ds.lon[i], w]);
      }
    });

    if (opts.heatByPrice && maxP > 0) {
      // normaliza em escala log — sem isso os spikes dominam o mapa inteiro
      var lm = Math.log(maxP + 1);
      for (var j = 0; j < pts.length; j++) {
        pts[j][2] = Math.max(0.08, Math.log(pts[j][2] + 1) / lm);
      }
    }

    return L.heatLayer(pts, {
      radius: 16,
      blur: 22,
      maxZoom: 15,
      minOpacity: 0.25,
      gradient: { 0.0: '#2b4b8f', 0.35: '#40a9a0', 0.6: '#f2c14a', 0.8: '#f08a3c', 1.0: '#e0453f' }
    });
  }

  function buildPoints(result, opts) {
    var group = L.layerGroup();
    var total = result.total;
    var limit = opts.pointLimit || 12000;
    var stride = total > limit ? Math.ceil(total / limit) : 1;
    pointsShown = 0;

    // Escala de cor por preço usa quantis do conjunto visível (moeda comum)
    var priceBreaks = opts.pointColorBy === 'price'
      ? Stats.quantileBreaks(result.commonValues, RAMP.length)
      : null;

    result.cities.forEach(function (city) {
      var ds = city.ds;
      var rtIndex = {};
      ds.roomTypes.forEach(function (t, i) { rtIndex[t] = i; });

      for (var k = 0; k < city.idx.length; k += stride) {
        var i = city.idx[k];
        var row = ds.rows[i];
        var pNative = Outliers.effectivePrice(ds, i, opts.outlierAction);
        var color;

        if (opts.pointColorBy === 'city') {
          color = ds.color;
        } else if (opts.pointColorBy === 'price') {
          if (pNative === null) color = '#67758e';
          else {
            var v = pNative * ds.fxRate, ci = 0;
            while (ci < priceBreaks.length && v > priceBreaks[ci]) ci++;
            color = RAMP[ci];
          }
        } else {
          color = CAT_COLORS[(rtIndex[row.roomType] || 0) % CAT_COLORS.length];
        }

        var isOut = ds.outlierMask[i] === 1;
        var m = L.circleMarker([ds.lat[i], ds.lon[i]], {
          renderer: canvasRenderer,
          radius: isOut && opts.outlierAction === 'highlight' ? 5 : 3.2,
          color: isOut && opts.outlierAction === 'highlight' ? '#ff5a5f' : color,
          weight: isOut && opts.outlierAction === 'highlight' ? 1.6 : 0.5,
          fillColor: color,
          fillOpacity: 0.78
        });

        m.bindPopup(buildPopup(ds, row, pNative, isOut));
        group.addLayer(m);
        pointsShown++;
      }
    });

    group._stride = stride;
    return group;
  }

  function buildPopup(ds, row, price, isOutlier) {
    var parts = [];
    parts.push('<b>' + escapeHtml(row.name || '(sem título)') + '</b>');
    parts.push('<span style="color:' + ds.color + '">●</span> ' + escapeHtml(ds.cityName) +
               (row.neighbourhood ? ' · ' + escapeHtml(row.neighbourhood) : ''));
    if (row.roomType) parts.push('Tipo: ' + escapeHtml(row.roomType));
    parts.push('Preço: <b>' + (price === null ? 'sem preço' : Stats.fmtMoney(price, ds.currency)) + '</b>' +
               (isOutlier ? ' <span style="color:#ff5a5f">⚠ outlier</span>' : ''));
    if (row.minNights !== null) parts.push('Mínimo de noites: ' + Stats.fmtNum(row.minNights));
    if (row.reviews !== null) parts.push('Avaliações: ' + Stats.fmtNum(row.reviews));
    if (row.availability !== null) parts.push('Disponibilidade: ' + Stats.fmtNum(row.availability) + ' dias/ano');
    if (row.hostName) parts.push('Anfitrião: ' + escapeHtml(row.hostName));
    return parts.join('<br>');
  }

  function escapeHtml(s) {
    return String(s === null || s === undefined ? '' : s)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  /* ---------------------------------------------------------- legenda */

  function renderLegend(result, opts) {
    if (!legendCtl || !legendCtl._div) return;
    var div = legendCtl._div;
    var html = '';

    if (opts.mapLayer === 'choropleth') {
      var isMoney = (opts.hexMetric === 'median' || opts.hexMetric === 'mean');
      var basis = moneyBasis(result, opts);
      var cur = basis.currency;
      html += '<div class="legend__title">' + METRIC_LABELS[opts.hexMetric] +
              (isMoney && cur ? ' (' + escapeHtml(cur) + ')' : '') + '</div>';
      html += '<div class="legend__scale">' +
              RAMP.map(function (c) { return '<i style="background:' + c + '"></i>'; }).join('') +
              '</div>';
      if (lastBreaks && lastBreaks.length) {
        html += '<div class="legend__ends"><span>' + fmtLegend(lastMin, isMoney, cur) +
                '</span><span>' + fmtLegend(lastMax, isMoney, cur) + '</span></div>';
        html += '<div class="legend__ends faint" style="margin-top:3px">quebras por quantil</div>';
      }
      if (isMoney && basis.convert) {
        html += '<div class="legend__ends faint" style="margin-top:4px">valores convertidos pela taxa de câmbio</div>';
      }
    } else if (opts.mapLayer === 'heat') {
      html += '<div class="legend__title">Densidade' + (opts.heatByPrice ? ' × preço' : '') + '</div>';
      html += '<div class="legend__scale">' +
              ['#2b4b8f', '#40a9a0', '#f2c14a', '#f08a3c', '#e0453f']
                .map(function (c) { return '<i style="background:' + c + '"></i>'; }).join('') +
              '</div><div class="legend__ends"><span>baixa</span><span>alta</span></div>';
    } else {
      html += '<div class="legend__title">Pontos</div>';
      if (opts.pointColorBy === 'city') {
        result.cities.forEach(function (c) {
          html += '<div class="legend__row"><i class="legend__swatch" style="background:' +
                  c.ds.color + '"></i>' + escapeHtml(c.ds.cityName) + '</div>';
        });
      } else if (opts.pointColorBy === 'price') {
        html += '<div class="legend__scale">' +
                RAMP.map(function (c) { return '<i style="background:' + c + '"></i>'; }).join('') +
                '</div><div class="legend__ends"><span>barato</span><span>caro</span></div>';
      } else {
        var seen = [];
        result.cities.forEach(function (c) {
          c.ds.roomTypes.forEach(function (t, i) {
            if (seen.indexOf(t) >= 0) return;
            seen.push(t);
            html += '<div class="legend__row"><i class="legend__swatch" style="background:' +
                    CAT_COLORS[i % CAT_COLORS.length] + '"></i>' + escapeHtml(t) + '</div>';
          });
        });
      }
      if (pointsShown < result.total) {
        html += '<div class="legend__ends" style="margin-top:5px;color:var(--warn)">amostra de ' +
                Stats.fmtNum(pointsShown) + ' de ' + Stats.fmtNum(result.total) + '</div>';
      }
    }

    div.innerHTML = html;
  }

  function fmtLegend(v, isMoney, cur) {
    return isMoney ? Stats.fmtMoney(v, cur) : Stats.fmtCompact(v);
  }

  var lastMin = 0, lastMax = 0;

  /* ---------------------------------------------------------- render */

  function render(result, opts) {
    if (!map) return;
    clearLayers();

    if (!result || !result.cities.length) {
      renderLegend({ cities: [], total: 0, commonValues: [], multiCurrency: false }, opts);
      return;
    }

    // O leaflet.heat quebra se o container ainda estiver com altura zero.
    var size = map.getSize();
    if (!size.y || !size.x) map.invalidateSize();

    try {
      if (opts.mapLayer === 'choropleth') {
        var built = buildChoropleth(result, opts);
        lastBreaks = built.breaks;
        lastMin = built.min;
        lastMax = built.max;
        layers.hex = built.layer.addTo(map);
      } else if (opts.mapLayer === 'heat') {
        layers.heat = buildHeat(result, opts).addTo(map);
      } else {
        layers.points = buildPoints(result, opts).addTo(map);
      }
    } catch (err) {
      console.error('[map] falha ao desenhar a camada ' + opts.mapLayer, err);
    }

    renderLegend(result, opts);
  }

  /** Enquadra todas as bases visíveis. */
  function fitToData(datasets, force) {
    if (!map || !datasets.length) return;
    if (hasFitted && !force) return;
    var b = L.latLngBounds([]);
    datasets.forEach(function (ds) {
      b.extend([ds.bounds.minLat, ds.bounds.minLon]);
      b.extend([ds.bounds.maxLat, ds.bounds.maxLon]);
    });
    if (b.isValid()) {
      map.fitBounds(b, { padding: [28, 28] });
      hasFitted = true;
    }
  }

  function fitToCity(ds) {
    if (!map || !ds) return;
    map.fitBounds(L.latLngBounds(
      [ds.bounds.minLat, ds.bounds.minLon],
      [ds.bounds.maxLat, ds.bounds.maxLon]
    ), { padding: [28, 28] });
  }

  function invalidate() { if (map) map.invalidateSize(); }

  global.MapView = {
    init: init,
    getMap: getMap,
    render: render,
    fitToData: fitToData,
    fitToCity: fitToCity,
    invalidate: invalidate,
    METRIC_LABELS: METRIC_LABELS,
    RAMP: RAMP,
    CAT_COLORS: CAT_COLORS,
    pointsShown: function () { return pointsShown; }
  };
})(window);
