/* ==========================================================================
   state.js — store central do dashboard (única fonte de verdade)
   Nenhum dado é embutido aqui: tudo entra via importação de CSV.
   ========================================================================== */
(function (global) {
  'use strict';

  /** Paleta usada para identificar cada cidade importada (por ordem de chegada). */
  var CITY_COLORS = ['#ff5a5f', '#00a699', '#4aa3ff', '#ffb400', '#c77dff', '#34c77b', '#ff8c42', '#7ec8e3'];

  var listeners = [];
  var seq = 0;

  var state = {
    /* Bases importadas. Cada item é produzido por csv-loader.js */
    datasets: [],

    /* Filtros aplicados sobre todas as bases visíveis */
    filters: {
      cities: null,          // null = todas; senão array de ids
      priceMin: null,        // valor absoluto na moeda da cidade OU convertido (ver options.compareInCommon)
      priceMax: null,
      neighbourhoods: [],    // [] = todos
      groups: [],            // boroughs / regiões (só onde a coluna existe)
      roomTypes: [],         // [] = todos
      minNightsMax: null,
      availMin: null,
      reviewsMin: null,
      search: '',
      includeNoPrice: false  // anúncios sem preço entram nas contagens?
    },

    /* Opções de análise e visualização */
    options: {
      /* Outliers */
      outlierMethod: 'iqr',      // 'iqr' | 'percentile' | 'mad' | 'none'
      outlierAction: 'remove',   // 'remove' | 'winsorize' | 'highlight'
      iqrK: 1.5,
      pLow: 1,
      pHigh: 99,
      madZ: 3.5,
      perRoomType: false,        // calcular limites por tipo de quarto

      /* Mapa */
      mapLayer: 'choropleth',    // 'choropleth' | 'heat' | 'points'
      hexRadiusKm: 0.6,
      hexMetric: 'median',       // 'median' | 'mean' | 'count' | 'availability' | 'reviews'
      colorScale: 'quantile',    // 'quantile' | 'linear' | 'log'
      pointColorBy: 'room',      // 'room' | 'price' | 'city'
      pointLimit: 12000,
      heatByPrice: true,

      /* Slider de preço */
      priceLogScale: true,       // distribuição é muito assimétrica; log ajuda a "arrastar"

      /* Comparação */
      compareInCommon: false,    // aplicar taxa de câmbio nas comparações
      commonCurrency: 'BRL',

      /* Export */
      emailTo: 'dante.darelli@hotmail.com'
    },

    /* Resultado corrente da filtragem — preenchido por filters.js */
    result: null,

    /* Chaves alteradas na última chamada de setOptions */
    changedOptions: [],

    ui: { ready: false }
  };

  function nextId(prefix) { seq += 1; return (prefix || 'ds') + '_' + seq; }

  function colorForIndex(i) { return CITY_COLORS[i % CITY_COLORS.length]; }

  /** Assina mudanças. Retorna função de cancelamento. */
  function subscribe(fn) {
    listeners.push(fn);
    return function () {
      var i = listeners.indexOf(fn);
      if (i >= 0) listeners.splice(i, 1);
    };
  }

  /**
   * Notifica todos os assinantes.
   * @param {string} reason - 'datasets' | 'filters' | 'options' | 'result' | 'init'
   */
  function emit(reason) {
    for (var i = 0; i < listeners.length; i++) {
      try { listeners[i](state, reason); }
      catch (err) { console.error('[state] assinante falhou (' + reason + ')', err); }
    }
  }

  function setFilters(patch, silent) {
    Object.assign(state.filters, patch);
    if (!silent) emit('filters');
  }

  function setOptions(patch, silent) {
    Object.assign(state.options, patch);
    // Quem reage precisa saber O QUE mudou: trocar a métrica do mapa não
    // deve reprocessar outliers nem refiltrar dezenas de milhares de linhas.
    state.changedOptions = Object.keys(patch);
    if (!silent) emit('options');
  }

  function addDataset(ds) {
    ds.id = ds.id || nextId();
    ds.color = ds.color || colorForIndex(state.datasets.length);
    ds.visible = ds.visible !== false;
    state.datasets.push(ds);
    emit('datasets');
    return ds;
  }

  function removeDataset(id) {
    var i = state.datasets.findIndex(function (d) { return d.id === id; });
    if (i < 0) return false;
    state.datasets.splice(i, 1);
    emit('datasets');
    return true;
  }

  function getDataset(id) {
    return state.datasets.find(function (d) { return d.id === id; }) || null;
  }

  /** Bases marcadas como visíveis (chips do header). */
  function activeDatasets() {
    return state.datasets.filter(function (d) { return d.visible; });
  }

  function toggleDataset(id, on) {
    var d = getDataset(id);
    if (!d) return;
    d.visible = (typeof on === 'boolean') ? on : !d.visible;
    emit('datasets');
  }

  function setResult(res) {
    state.result = res;
    emit('result');
  }

  global.Store = {
    state: state,
    subscribe: subscribe,
    emit: emit,
    setFilters: setFilters,
    setOptions: setOptions,
    addDataset: addDataset,
    removeDataset: removeDataset,
    getDataset: getDataset,
    activeDatasets: activeDatasets,
    toggleDataset: toggleDataset,
    setResult: setResult,
    nextId: nextId,
    CITY_COLORS: CITY_COLORS
  };
})(window);
