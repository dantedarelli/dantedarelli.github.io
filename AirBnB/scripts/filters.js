/* ==========================================================================
   filters.js — motor de filtragem + slider duplo "drag in"
   Todos os preços comparáveis passam pela taxa de câmbio da própria base
   (fxRate, editável na UI). Com fxRate = 1 em todas, o valor é o bruto.
   ========================================================================== */
(function (global) {
  'use strict';

  var norm = Stats.normalizeText;

  /* ======================================================================
     1) Motor de filtragem
     ====================================================================== */

  /** Preço convertido para a moeda comum, já com tratamento de outlier. */
  function convertedPrice(ds, i, action) {
    var p = Outliers.effectivePrice(ds, i, action);
    return p === null ? null : p * ds.fxRate;
  }

  /**
   * Domínio de preço das bases ativas (ignora o próprio filtro de preço).
   * Alimenta os limites e o histograma do slider.
   */
  function priceDomain() {
    var st = global.Store.state;
    var opts = st.options;
    var vals = [];
    global.Store.activeDatasets().forEach(function (ds) {
      for (var i = 0; i < ds.count; i++) {
        var p = convertedPrice(ds, i, opts.outlierAction);
        if (p !== null) vals.push(p);
      }
    });
    if (!vals.length) return { min: 0, max: 0, values: [] };
    var min = Infinity, max = -Infinity;
    for (var k = 0; k < vals.length; k++) {
      if (vals[k] < min) min = vals[k];
      if (vals[k] > max) max = vals[k];
    }
    return { min: min, max: max, values: vals };
  }

  function matchesSearch(row, needle) {
    if (!needle) return true;
    return norm(row.name).indexOf(needle) >= 0 ||
           norm(row.neighbourhood).indexOf(needle) >= 0 ||
           norm(row.hostName).indexOf(needle) >= 0;
  }

  /** Executa todos os filtros e publica o resultado no Store. */
  function compute() {
    var st = global.Store.state;
    var f = st.filters;
    var opts = st.options;
    var needle = norm(f.search);

    var nbSet = f.neighbourhoods.length ? new Set(f.neighbourhoods) : null;
    var grpSet = f.groups.length ? new Set(f.groups) : null;
    var rtSet = f.roomTypes.length ? new Set(f.roomTypes) : null;
    var citySet = (f.cities && f.cities.length) ? new Set(f.cities) : null;

    var cities = [];
    var allCommon = [];
    var grandTotal = 0;
    var grandOutliers = 0;

    global.Store.activeDatasets().forEach(function (ds) {
      if (citySet && !citySet.has(ds.id)) return;

      var idx = [];
      var native = [];
      var common = [];
      var byNb = new Map();
      var byRt = new Map();
      var nbPrices = new Map();
      var flagged = 0;
      var noPriceKept = 0;
      var availSum = 0, availN = 0;
      var nightsSum = 0, nightsN = 0;
      var reviewsSum = 0, reviewsN = 0;

      for (var i = 0; i < ds.count; i++) {
        var row = ds.rows[i];

        if (nbSet && !nbSet.has(row.neighbourhood)) continue;
        if (grpSet && !grpSet.has(row.group)) continue;
        if (rtSet && !rtSet.has(row.roomType)) continue;
        if (f.minNightsMax !== null && row.minNights !== null && row.minNights > f.minNightsMax) continue;
        if (f.availMin !== null && (row.availability === null || row.availability < f.availMin)) continue;
        if (f.reviewsMin !== null && (row.reviews === null || row.reviews < f.reviewsMin)) continue;
        if (needle && !matchesSearch(row, needle)) continue;

        var pEff = Outliers.effectivePrice(ds, i, opts.outlierAction);

        if (pEff === null) {
          // sem preço, ou spike removido pelo tratamento de outliers
          if (ds.priceValid[i]) continue;            // era outlier removido → fora
          if (!f.includeNoPrice) continue;           // anúncio sem preço → só se pedido
          noPriceKept++;
        } else {
          var pC = pEff * ds.fxRate;
          if (f.priceMin !== null && pC < f.priceMin) continue;
          if (f.priceMax !== null && pC > f.priceMax) continue;
          native.push(pEff);
          common.push(pC);
          allCommon.push(pC);
          if (row.neighbourhood) {
            var arr = nbPrices.get(row.neighbourhood);
            if (!arr) { arr = []; nbPrices.set(row.neighbourhood, arr); }
            arr.push(pEff);
          }
        }

        if (ds.outlierMask[i]) flagged++;
        idx.push(i);

        if (row.neighbourhood) byNb.set(row.neighbourhood, (byNb.get(row.neighbourhood) || 0) + 1);
        if (row.roomType) byRt.set(row.roomType, (byRt.get(row.roomType) || 0) + 1);
        if (row.availability !== null) { availSum += row.availability; availN++; }
        if (row.minNights !== null) { nightsSum += row.minNights; nightsN++; }
        if (row.reviews !== null) { reviewsSum += row.reviews; reviewsN++; }
      }

      grandTotal += idx.length;
      grandOutliers += flagged;

      cities.push({
        ds: ds,
        idx: idx,
        count: idx.length,
        native: native,
        common: common,
        noPriceKept: noPriceKept,
        outliersInView: flagged,
        stats: Stats.describe(native),
        statsCommon: Stats.describe(common),
        byNeighbourhood: byNb,
        byRoomType: byRt,
        nbPrices: nbPrices,
        avgAvailability: availN ? availSum / availN : NaN,
        avgMinNights: nightsN ? nightsSum / nightsN : NaN,
        avgReviews: reviewsN ? reviewsSum / reviewsN : NaN,
        occupancy: availN ? (365 - availSum / availN) / 365 : NaN
      });
    });

    var result = {
      cities: cities,
      total: grandTotal,
      totalWithPrice: allCommon.length,
      outliersInView: grandOutliers,
      commonStats: Stats.describe(allCommon),
      commonValues: allCommon,
      multiCurrency: new Set(cities.map(function (c) { return c.ds.currency; })).size > 1,
      generatedAt: new Date()
    };

    global.Store.setResult(result);
    return result;
  }

  /**
   * Agenda a filtragem para o próximo frame (usado durante o arraste).
   * O rAF é acompanhado de um timer de segurança: em aba oculta o navegador
   * congela o rAF e o recálculo nunca aconteceria.
   */
  var rafId = null, timerId = null;

  function runPending() {
    if (rafId !== null) { cancelAnimationFrame(rafId); rafId = null; }
    if (timerId !== null) { clearTimeout(timerId); timerId = null; }
    compute();
  }

  function scheduleCompute() {
    if (rafId !== null || timerId !== null) return;
    rafId = requestAnimationFrame(runPending);
    timerId = setTimeout(runPending, 120);
  }

  /** Força o recálculo imediato, descartando qualquer agendamento pendente. */
  function computeNow() { runPending(); }

  /* ======================================================================
     2) Slider duplo "drag in"
     ====================================================================== */

  /**
   * Slider de duas alças com escala linear ou logarítmica, teclado e
   * campos numéricos nas pontas.
   *
   * @param {HTMLElement} el     container .range
   * @param {object} cfg { onChange(min,max,final), log }
   */
  function RangeSlider(el, cfg) {
    this.el = el;
    this.cfg = cfg || {};
    this.min = 0; this.max = 100;
    this.lo = 0; this.hi = 100;
    this.log = !!this.cfg.log;

    el.innerHTML =
      '<div class="range__hist"></div>' +
      '<div class="range__track"></div>' +
      '<div class="range__fill"></div>' +
      '<div class="range__thumb range__thumb--min" tabindex="0" role="slider" aria-label="Preço mínimo"></div>' +
      '<div class="range__thumb range__thumb--max" tabindex="0" role="slider" aria-label="Preço máximo"></div>';

    this.$hist = el.querySelector('.range__hist');
    this.$lo = el.querySelector('.range__thumb--min');
    this.$hi = el.querySelector('.range__thumb--max');

    this._bind();
  }

  /** valor → 0..1 respeitando a escala escolhida */
  RangeSlider.prototype.toPos = function (v) {
    if (this.max <= this.min) return 0;
    if (this.log) {
      var a = Math.log(Math.max(1, this.min));
      var b = Math.log(Math.max(1, this.max));
      if (b <= a) return 0;
      return (Math.log(Math.max(1, v)) - a) / (b - a);
    }
    return (v - this.min) / (this.max - this.min);
  };

  /** 0..1 → valor */
  RangeSlider.prototype.toValue = function (t) {
    t = Math.min(1, Math.max(0, t));
    if (this.log) {
      var a = Math.log(Math.max(1, this.min));
      var b = Math.log(Math.max(1, this.max));
      return Math.exp(a + t * (b - a));
    }
    return this.min + t * (this.max - this.min);
  };

  RangeSlider.prototype.setDomain = function (min, max, log) {
    this.min = min;
    this.max = max > min ? max : min + 1;
    if (typeof log === 'boolean') this.log = log;
    this.lo = Math.max(this.min, Math.min(this.lo, this.max));
    this.hi = Math.max(this.min, Math.min(this.hi, this.max));
    if (this.hi <= this.lo) { this.lo = this.min; this.hi = this.max; }
    this.render();
  };

  RangeSlider.prototype.setValues = function (lo, hi, silent) {
    this.lo = Math.max(this.min, Math.min(lo, this.max));
    this.hi = Math.max(this.lo, Math.min(hi, this.max));
    this.render();
    if (!silent && this.cfg.onChange) this.cfg.onChange(this.lo, this.hi, true);
  };

  RangeSlider.prototype.reset = function (silent) {
    this.setValues(this.min, this.max, silent);
  };

  /** Mini-histograma de fundo (mostra onde a massa de anúncios está). */
  RangeSlider.prototype.setHistogram = function (values, bars) {
    var n = bars || 44;
    var counts = new Array(n).fill(0);
    if (values && values.length && this.max > this.min) {
      for (var i = 0; i < values.length; i++) {
        var t = this.toPos(values[i]);
        var b = Math.floor(t * n);
        if (b < 0) b = 0; if (b >= n) b = n - 1;
        counts[b]++;
      }
    }
    var peak = Math.max.apply(null, counts) || 1;
    var html = '';
    for (var k = 0; k < n; k++) {
      var h = Math.round((counts[k] / peak) * 100);
      html += '<i style="height:' + Math.max(2, h) + '%"></i>';
    }
    this.$hist.innerHTML = html;
    this._bars = this.$hist.querySelectorAll('i');
    this.render();
  };

  RangeSlider.prototype.render = function () {
    var pLo = this.toPos(this.lo) * 100;
    var pHi = this.toPos(this.hi) * 100;
    this.el.style.setProperty('--pct-min', pLo + '%');
    this.el.style.setProperty('--pct-max', pHi + '%');
    this.$lo.setAttribute('aria-valuenow', Math.round(this.lo));
    this.$hi.setAttribute('aria-valuenow', Math.round(this.hi));

    if (this._bars) {
      var n = this._bars.length;
      for (var i = 0; i < n; i++) {
        var center = ((i + 0.5) / n) * 100;
        this._bars[i].classList.toggle('in', center >= pLo && center <= pHi);
      }
    }
  };

  RangeSlider.prototype._posFromEvent = function (ev) {
    var rect = this.el.getBoundingClientRect();
    var x = (ev.touches ? ev.touches[0].clientX : ev.clientX) - rect.left;
    return rect.width ? x / rect.width : 0;
  };

  RangeSlider.prototype._bind = function () {
    var self = this;
    var dragging = null;

    function start(which) {
      return function (ev) {
        ev.preventDefault();
        dragging = which;
        (which === 'lo' ? self.$lo : self.$hi).focus();
        move(ev);
        window.addEventListener('pointermove', move);
        window.addEventListener('pointerup', end);
      };
    }

    function move(ev) {
      if (!dragging) return;
      var v = self.toValue(self._posFromEvent(ev));
      if (dragging === 'lo') self.lo = Math.min(v, self.hi);
      else self.hi = Math.max(v, self.lo);
      self.render();
      if (self.cfg.onChange) self.cfg.onChange(self.lo, self.hi, false);
    }

    function end() {
      if (!dragging) return;
      dragging = null;
      window.removeEventListener('pointermove', move);
      window.removeEventListener('pointerup', end);
      if (self.cfg.onChange) self.cfg.onChange(self.lo, self.hi, true);
    }

    this.$lo.addEventListener('pointerdown', start('lo'));
    this.$hi.addEventListener('pointerdown', start('hi'));

    // Clicar na trilha move a alça mais próxima
    this.el.addEventListener('pointerdown', function (ev) {
      if (ev.target === self.$lo || ev.target === self.$hi) return;
      var v = self.toValue(self._posFromEvent(ev));
      var toLo = Math.abs(self.toPos(v) - self.toPos(self.lo));
      var toHi = Math.abs(self.toPos(v) - self.toPos(self.hi));
      start(toLo <= toHi ? 'lo' : 'hi')(ev);
    });

    // Teclado
    function keys(which) {
      return function (ev) {
        var step = (self.max - self.min) / 100;
        var big = step * 10;
        var delta = 0;
        if (ev.key === 'ArrowLeft' || ev.key === 'ArrowDown') delta = -step;
        else if (ev.key === 'ArrowRight' || ev.key === 'ArrowUp') delta = step;
        else if (ev.key === 'PageDown') delta = -big;
        else if (ev.key === 'PageUp') delta = big;
        else if (ev.key === 'Home') delta = -Infinity;
        else if (ev.key === 'End') delta = Infinity;
        else return;
        ev.preventDefault();

        if (which === 'lo') {
          self.lo = Math.max(self.min, Math.min(self.lo + delta, self.hi));
        } else {
          self.hi = Math.min(self.max, Math.max(self.hi + delta, self.lo));
        }
        self.render();
        if (self.cfg.onChange) self.cfg.onChange(self.lo, self.hi, true);
      };
    }
    this.$lo.addEventListener('keydown', keys('lo'));
    this.$hi.addEventListener('keydown', keys('hi'));
  };

  global.Filters = {
    compute: compute,
    scheduleCompute: scheduleCompute,
    computeNow: computeNow,
    priceDomain: priceDomain,
    convertedPrice: convertedPrice,
    RangeSlider: RangeSlider
  };
})(window);
