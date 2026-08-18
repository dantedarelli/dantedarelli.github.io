/* ==========================================================================
   app.js — bootstrap e orquestração
   Liga os controles da interface ao store, ao motor de filtros e às camadas
   de visualização. Nenhum dado nasce aqui: tudo vem da importação de CSV.
   ========================================================================== */
(function (global) {
  'use strict';

  var $ = function (id) { return document.getElementById(id); };
  var Store, opts, filters;
  var priceSlider = null;
  var domain = { min: 0, max: 0, values: [] };
  var dragging = false;

  var OUTLIER_KEYS = ['outlierMethod', 'outlierAction', 'iqrK', 'pLow', 'pHigh', 'madZ', 'perRoomType'];
  var MAP_KEYS = ['mapLayer', 'hexRadiusKm', 'hexMetric', 'colorScale', 'pointColorBy', 'pointLimit', 'heatByPrice'];
  var COMPARE_KEYS = ['compareInCommon', 'commonCurrency'];

  /* ====================== Ciclo de atualização ======================= */

  /** Reaplica outliers, recalcula domínio do slider e refiltra. */
  function onDataChanged(fit) {
    Outliers.applyAll(Store.state.datasets, opts);
    refreshPriceDomain(true);
    UI.renderCityChips();
    UI.renderFilterLists();
    UI.renderOutlierInfo(opts);
    Filters.compute();
    if (fit) {
      MapView.invalidate();
      MapView.fitToData(Store.activeDatasets(), true);
    }
  }

  /** Recalcula limites e histograma do slider de preço. */
  function refreshPriceDomain(resetValues) {
    domain = Filters.priceDomain();
    if (!priceSlider) return;

    var min = Math.floor(domain.min);
    var max = Math.ceil(domain.max);
    if (!isFinite(min) || !isFinite(max) || max <= min) { min = 0; max = 1; }

    priceSlider.setDomain(min, max, opts.priceLogScale);
    priceSlider.setHistogram(domain.values);

    if (resetValues) {
      priceSlider.setValues(min, max, true);
      filters.priceMin = null;
      filters.priceMax = null;
      syncPriceInputs(min, max);
    }
    updatePriceHint();
  }

  function syncPriceInputs(lo, hi) {
    $('fPriceMin').value = Math.round(lo);
    $('fPriceMax').value = Math.round(hi);
  }

  function updatePriceHint() {
    var cur = currentCurrencyLabel();
    $('priceRangeHint').textContent =
      'Domínio: ' + Stats.fmtMoney(domain.min, cur) + ' a ' + Stats.fmtMoney(domain.max, cur) +
      ' · ' + Stats.fmtNum(domain.values.length) + ' anúncios com preço';
  }

  /** Moeda usada no slider: a da única base ativa, ou a moeda comum. */
  function currentCurrencyLabel() {
    var act = Store.activeDatasets();
    if (act.length === 1) return act[0].currency;
    return opts.commonCurrency;
  }

  /** Aplica os valores do slider aos filtros. */
  function applyPriceRange(lo, hi, final) {
    var atMin = lo <= domain.min + 1e-6;
    var atMax = hi >= domain.max - 1e-6;
    filters.priceMin = atMin ? null : lo;
    filters.priceMax = atMax ? null : hi;
    syncPriceInputs(lo, hi);
    dragging = !final;
    // Durante o arraste basta acompanhar os cards; ao soltar, recalcula tudo.
    if (final) Filters.computeNow();
    else Filters.scheduleCompute();
  }

  /* ============================ Renderização ========================= */

  function renderAll(result) {
    UI.renderCards(result, opts);
    UI.renderActiveFilters(result, opts);
    UI.renderExportSummary(result, opts);

    if (dragging) return;   // durante o arraste só os cards acompanham

    MapView.render(result, opts);
    Charts.renderAll(result, opts);
    Compare.render(result, opts);
  }

  /* ======================= Controles: importação ===================== */

  function bindImport() {
    var dz = $('dropzone');
    var input = $('fileInput');

    dz.addEventListener('click', function () { input.click(); });
    dz.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); input.click(); }
    });
    ['dragenter', 'dragover'].forEach(function (ev) {
      dz.addEventListener(ev, function (e) { e.preventDefault(); dz.classList.add('is-over'); });
    });
    ['dragleave', 'drop'].forEach(function (ev) {
      dz.addEventListener(ev, function (e) { e.preventDefault(); dz.classList.remove('is-over'); });
    });
    dz.addEventListener('drop', function (e) {
      if (e.dataTransfer && e.dataTransfer.files) UI.addFiles(e.dataTransfer.files);
    });
    input.addEventListener('change', function () { UI.addFiles(input.files); });

    // Campos por arquivo na fila (nome da cidade, moeda, câmbio, remoção)
    $('importList').addEventListener('input', function (e) {
      var q = UI.getQueue();
      var t = e.target;
      if (t.dataset.city !== undefined) q[+t.dataset.city].cityName = t.value.trim();
      else if (t.dataset.cur !== undefined) q[+t.dataset.cur].currency = t.value.trim().toUpperCase();
      else if (t.dataset.fx !== undefined) {
        var v = parseFloat(t.value);
        q[+t.dataset.fx].fxRate = (isFinite(v) && v > 0) ? v : 1;
      }
    });
    $('importList').addEventListener('click', function (e) {
      var rm = e.target.dataset.rm;
      if (rm !== undefined) {
        UI.getQueue().splice(+rm, 1);
        UI.renderQueue();
      }
    });

    // Mapeamento manual de colunas
    $('mappingFields').addEventListener('change', function (e) {
      var t = e.target;
      if (t.dataset.role === undefined) return;
      var entry = UI.getQueue()[+t.dataset.entry];
      if (!entry) return;
      if (t.value) entry.map[t.dataset.role] = t.value;
      else delete entry.map[t.dataset.role];

      entry.missing = CsvLoader.ROLES
        .filter(function (r) { return r.required && !entry.map[r.key]; })
        .map(function (r) { return r.key; });

      var pending = UI.getQueue().filter(function (x) { return !x.error; });
      $('importConfirm').disabled = !pending.length ||
        pending.some(function (x) { return !x.headers || x.missing.length; });
    });

    $('importConfirm').addEventListener('click', UI.runImport);
    $('importCancel').addEventListener('click', UI.closeImport);
    $('importClose').addEventListener('click', UI.closeImport);
    $('btnImport').addEventListener('click', function () { UI.openImport(false); });
  }

  /* ========================= Controles: cidades ====================== */

  function bindCityChips() {
    $('cityChips').addEventListener('click', function (e) {
      var rm = e.target.dataset.remove;
      if (rm) {
        e.stopPropagation();
        var d = Store.getDataset(rm);
        if (d && confirm('Remover a base "' + d.cityName + '" do dashboard?')) {
          Store.removeDataset(rm);
          if (!Store.state.datasets.length) UI.openImport(true);
        }
        return;
      }
      var chip = e.target.closest('[data-city]');
      if (chip) Store.toggleDataset(chip.dataset.city);
    });
  }

  /* ========================= Controles: filtros ====================== */

  function bindFilters() {
    priceSlider = new Filters.RangeSlider($('priceRange'), {
      log: true,
      onChange: function (lo, hi, final) { applyPriceRange(lo, hi, final); }
    });

    $('fPriceLog').addEventListener('change', function () {
      Store.setOptions({ priceLogScale: this.checked }, true);
      priceSlider.setDomain(domain.min, domain.max, this.checked);
      priceSlider.setHistogram(domain.values);
    });

    ['fPriceMin', 'fPriceMax'].forEach(function (id) {
      $(id).addEventListener('change', function () {
        var lo = parseFloat($('fPriceMin').value);
        var hi = parseFloat($('fPriceMax').value);
        if (!isFinite(lo)) lo = domain.min;
        if (!isFinite(hi)) hi = domain.max;
        if (hi < lo) { var t = lo; lo = hi; hi = t; }
        priceSlider.setValues(lo, hi, true);
        applyPriceRange(priceSlider.lo, priceSlider.hi, true);
      });
    });

    var searchTimer = null;
    $('fSearch').addEventListener('input', function () {
      var v = this.value;
      clearTimeout(searchTimer);
      searchTimer = setTimeout(function () {
        Store.setFilters({ search: v });
      }, 260);
    });

    $('fIncludeNoPrice').addEventListener('change', function () {
      Store.setFilters({ includeNoPrice: this.checked });
    });

    // Listas de checkbox (tipo, região, bairro)
    ['fRoomTypes', 'fGroups', 'fNeighbourhoods'].forEach(function (id) {
      $(id).addEventListener('change', function (e) {
        var name = e.target.dataset.filter;
        if (!name) return;
        var checked = Array.prototype.map.call(
          this.querySelectorAll('input[data-filter="' + name + '"]:checked'),
          function (i) { return i.value; }
        );
        // preserva seleções de bairros que foram filtradas fora da lista visível
        if (name === 'neighbourhoods') {
          var visible = new Set(Array.prototype.map.call(
            this.querySelectorAll('input[data-filter="neighbourhoods"]'),
            function (i) { return i.value; }
          ));
          filters.neighbourhoods.forEach(function (v) {
            if (!visible.has(v) && checked.indexOf(v) < 0) checked.push(v);
          });
        }
        var patch = {};
        patch[name] = checked;
        Store.setFilters(patch);
      });
    });

    var nbTimer = null;
    $('fNbSearch').addEventListener('input', function () {
      clearTimeout(nbTimer);
      nbTimer = setTimeout(UI.renderFilterLists, 200);
    });
    $('btnClearNb').addEventListener('click', function () {
      $('fNbSearch').value = '';
      Store.setFilters({ neighbourhoods: [] });
      UI.renderFilterLists();
    });

    // Sliders simples
    bindRange('fMinNights', 'fMinNightsVal', function (v, el) {
      var max = +el.max;
      var val = (v >= max) ? null : v;
      Store.setFilters({ minNightsMax: val });
      return val === null ? 'todos' : '≤ ' + v;
    });
    bindRange('fAvail', 'fAvailVal', function (v) {
      Store.setFilters({ availMin: v || null });
      return v ? v + ' dias' : '0';
    });
    bindRange('fReviews', 'fReviewsVal', function (v) {
      Store.setFilters({ reviewsMin: v || null });
      return String(v);
    });

    $('btnResetFilters').addEventListener('click', resetFilters);

    // Remoção de filtro pelos chips
    $('activeFilters').addEventListener('click', function (e) {
      var key = e.target.dataset.clear;
      if (!key) return;
      var patch = {};
      if (key === 'search') { patch.search = ''; $('fSearch').value = ''; }
      else if (key === 'roomTypes' || key === 'groups' || key === 'neighbourhoods') patch[key] = [];
      else if (key === 'minNightsMax') { patch.minNightsMax = null; $('fMinNights').value = $('fMinNights').max; $('fMinNightsVal').textContent = 'todos'; }
      else if (key === 'availMin') { patch.availMin = null; $('fAvail').value = 0; $('fAvailVal').textContent = '0'; }
      else if (key === 'reviewsMin') { patch.reviewsMin = null; $('fReviews').value = 0; $('fReviewsVal').textContent = '0'; }
      Store.setFilters(patch);
      UI.renderFilterLists();
    });
  }

  function bindRange(id, labelId, handler) {
    var el = $(id);
    var lbl = $(labelId);
    var timer = null;
    el.addEventListener('input', function () {
      var v = +el.value;
      clearTimeout(timer);
      timer = setTimeout(function () {
        var text = handler(v, el);
        if (text !== undefined) lbl.textContent = text;
      }, 130);
    });
  }

  function resetFilters() {
    Store.setFilters({
      neighbourhoods: [], groups: [], roomTypes: [],
      minNightsMax: null, availMin: null, reviewsMin: null,
      search: '', includeNoPrice: false, priceMin: null, priceMax: null
    }, true);

    $('fSearch').value = '';
    $('fNbSearch').value = '';
    $('fIncludeNoPrice').checked = false;
    $('fMinNights').value = $('fMinNights').max; $('fMinNightsVal').textContent = 'todos';
    $('fAvail').value = 0; $('fAvailVal').textContent = '0';
    $('fReviews').value = 0; $('fReviewsVal').textContent = '0';

    refreshPriceDomain(true);
    UI.renderFilterLists();
    Filters.compute();
    UI.toast('Filtros limpos.', 'ok');
  }

  /* ======================== Controles: outliers ====================== */

  function bindOutliers() {
    $('oMethod').addEventListener('change', function () {
      var m = this.value;
      $('oParamIqr').classList.toggle('hidden', m !== 'iqr');
      $('oParamPct').classList.toggle('hidden', m !== 'percentile');
      $('oParamMad').classList.toggle('hidden', m !== 'mad');
      Store.setOptions({ outlierMethod: m });
    });

    $('oAction').addEventListener('click', function (e) {
      var b = e.target.closest('button[data-v]');
      if (!b) return;
      this.querySelectorAll('button').forEach(function (x) { x.classList.remove('is-active'); });
      b.classList.add('is-active');
      Store.setOptions({ outlierAction: b.dataset.v });
    });

    var t1 = null;
    $('oIqrK').addEventListener('input', function () {
      var v = parseFloat(this.value);
      $('oIqrKVal').textContent = Stats.fmtNum(v, 1);
      clearTimeout(t1);
      t1 = setTimeout(function () { Store.setOptions({ iqrK: v }); }, 170);
    });

    var t2 = null;
    $('oMadZ').addEventListener('input', function () {
      var v = parseFloat(this.value);
      $('oMadZVal').textContent = Stats.fmtNum(v, 1);
      clearTimeout(t2);
      t2 = setTimeout(function () { Store.setOptions({ madZ: v }); }, 170);
    });

    ['oPLow', 'oPHigh'].forEach(function (id) {
      $(id).addEventListener('change', function () {
        Store.setOptions({
          pLow: parseFloat($('oPLow').value) || 0,
          pHigh: parseFloat($('oPHigh').value) || 100
        });
      });
    });

    $('oPerRoom').addEventListener('change', function () {
      Store.setOptions({ perRoomType: this.checked });
    });
  }

  /* ========================== Controles: mapa ======================== */

  function bindMap() {
    $('mapLayerSeg').addEventListener('click', function (e) {
      var b = e.target.closest('button[data-v]');
      if (!b) return;
      this.querySelectorAll('button').forEach(function (x) { x.classList.remove('is-active'); });
      b.classList.add('is-active');
      var v = b.dataset.v;
      $('mapOptsChoropleth').classList.toggle('hidden', v !== 'choropleth');
      $('mapOptsPoints').classList.toggle('hidden', v !== 'points');
      $('mapOptsHeat').classList.toggle('hidden', v !== 'heat');
      Store.setOptions({ mapLayer: v });
    });

    $('mapMetric').addEventListener('change', function () {
      Store.setOptions({ hexMetric: this.value });
    });
    $('mapScale').addEventListener('change', function () {
      Store.setOptions({ colorScale: this.value });
    });
    $('mapPointColor').addEventListener('change', function () {
      Store.setOptions({ pointColorBy: this.value });
    });

    var t = null;
    $('mapHexR').addEventListener('input', function () {
      var v = parseFloat(this.value);
      $('mapHexRVal').textContent = Stats.fmtNum(v, 2) + ' km';
      clearTimeout(t);
      t = setTimeout(function () { Store.setOptions({ hexRadiusKm: v }); }, 230);
    });

    var t2 = null;
    $('mapPointLimit').addEventListener('input', function () {
      var v = parseInt(this.value, 10);
      $('mapPointLimitVal').textContent = Stats.fmtNum(v);
      clearTimeout(t2);
      t2 = setTimeout(function () { Store.setOptions({ pointLimit: v }); }, 230);
    });

    $('mapHeatPrice').addEventListener('change', function () {
      Store.setOptions({ heatByPrice: this.checked });
    });

    $('btnFit').addEventListener('click', function () {
      MapView.fitToData(Store.activeDatasets(), true);
    });
  }

  /* ======================== Controles: comparação ==================== */

  function bindCompare() {
    var panel = document.querySelector('.compare-grid');
    if (!panel) return;

    panel.addEventListener('change', function (e) {
      var t = e.target;

      if (t.id === 'cmpCommon') {
        Store.setOptions({ compareInCommon: t.checked });
        return;
      }
      if (t.id === 'cmpCommonCur') {
        Store.setOptions({ commonCurrency: t.value.trim().toUpperCase() || 'BRL' });
        return;
      }
      if (t.dataset.fxCur !== undefined) {
        var d1 = Store.getDataset(t.dataset.fxCur);
        if (d1) { d1.currency = t.value.trim().toUpperCase(); Store.emit('datasets'); }
        return;
      }
      if (t.dataset.fxRate !== undefined) {
        var d2 = Store.getDataset(t.dataset.fxRate);
        var v = parseFloat(t.value);
        if (d2 && isFinite(v) && v > 0) {
          d2.fxRate = v;
          refreshPriceDomain(true);
          Filters.compute();
          UI.toast('Taxa de câmbio de ' + UI.esc(d2.cityName) + ' atualizada para ' + v + '.', 'ok');
        }
      }
    });
  }

  /* ========================= Controles: export ======================= */

  function bindExport() {
    function openExport() {
      UI.renderExportSummary(Store.state.result, opts);
      $('exportModal').classList.remove('hidden');
    }

    $('btnExport').addEventListener('click', openExport);
    $('btnEmail').addEventListener('click', openExport);

    document.querySelectorAll('[data-close="exportModal"]').forEach(function (b) {
      b.addEventListener('click', function () { $('exportModal').classList.add('hidden'); });
    });

    $('btnExportDetail').addEventListener('click', function () {
      var n = Exporter.exportDetail(Store.state.result, opts);
      UI.toast(n ? Stats.fmtNum(n) + ' registros exportados em CSV.' : 'Nada a exportar.', n ? 'ok' : 'warn');
    });

    $('btnExportSummary').addEventListener('click', function () {
      var n = Exporter.exportSummary(Store.state.result, opts);
      UI.toast(n ? 'Resumo de ' + n + ' cidade(s) exportado.' : 'Nada a exportar.', n ? 'ok' : 'warn');
    });

    $('btnCopySummary').addEventListener('click', function () {
      Exporter.copySummary(Store.state.result, opts, UI.describeFilters(opts))
        .then(function () { UI.toast('Resumo copiado para a área de transferência.', 'ok'); })
        .catch(function (err) { UI.toast(UI.esc(err.message || err), 'error'); });
    });

    $('emailTo').addEventListener('change', function () {
      Store.setOptions({ emailTo: this.value.trim() }, true);
    });

    $('btnSendEmail').addEventListener('click', function () {
      var to = $('emailTo').value.trim();
      if (!to) { UI.toast('Informe o destinatário.', 'warn'); return; }
      var r = Exporter.sendByEmail(Store.state.result, opts, UI.describeFilters(opts), to);
      if (r) {
        UI.toast('CSV baixado (' + Stats.fmtNum(r.rows) + ' registros) e cliente de e-mail aberto. ' +
                 'Anexe o arquivo antes de enviar.', 'ok', 'E-mail preparado');
      } else {
        UI.toast('Nada a enviar — importe uma base primeiro.', 'warn');
      }
    });
  }

  /* ============================== Tema =============================== */

  function bindTheme() {
    $('btnTheme').addEventListener('click', function () {
      var root = document.documentElement;
      var next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      root.setAttribute('data-theme', next);
      if (Store.state.result) {
        Charts.renderAll(Store.state.result, opts);
        Compare.render(Store.state.result, opts);
      }
    });
  }

  /* ============================== Bootstrap ========================== */

  function boot() {
    Store = global.Store;
    opts = Store.state.options;
    filters = Store.state.filters;

    MapView.init('map');

    bindImport();
    bindCityChips();
    bindFilters();
    bindOutliers();
    bindMap();
    bindCompare();
    bindExport();
    bindTheme();

    Store.subscribe(function (state, reason) {
      if (reason === 'datasets') {
        Outliers.applyAll(state.datasets, opts);
        UI.renderCityChips();
        UI.renderFilterLists();
        UI.renderOutlierInfo(opts);
        refreshPriceDomain(true);
        Filters.compute();
      } else if (reason === 'options') {
        var changed = state.changedOptions || [];
        var touchesOutliers = changed.some(function (k) { return OUTLIER_KEYS.indexOf(k) >= 0; });
        var mapOnly = changed.length && changed.every(function (k) { return MAP_KEYS.indexOf(k) >= 0; });
        var compareOnly = changed.length && changed.every(function (k) { return COMPARE_KEYS.indexOf(k) >= 0; });

        if (mapOnly) {
          MapView.render(state.result, opts);            // redesenha só o mapa
        } else if (compareOnly) {
          Compare.render(state.result, opts);
          UI.renderCards(state.result, opts);
          MapView.render(state.result, opts);            // a escala de cor usa o câmbio
        } else {
          if (touchesOutliers) {
            Outliers.applyAll(state.datasets, opts);
            UI.renderOutlierInfo(opts);
            refreshPriceDomain(false);
          }
          Filters.compute();
        }
      } else if (reason === 'filters') {
        Filters.compute();
      } else if (reason === 'result') {
        renderAll(state.result);
        dragging = false;
      }
    });

    // Requisito: ao abrir, o dashboard pede a importação das tabelas.
    UI.openImport(true);
    UI.renderCards(null, opts);

    window.addEventListener('resize', function () { MapView.invalidate(); });

    document.addEventListener('keydown', function (e) {
      if (e.key !== 'Escape') return;
      if (!$('exportModal').classList.contains('hidden')) $('exportModal').classList.add('hidden');
      else if (!$('importModal').classList.contains('hidden')) UI.closeImport();
    });
  }

  global.App = { boot: boot, onDataChanged: onDataChanged, refreshPriceDomain: refreshPriceDomain };

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
  else boot();
})(window);
