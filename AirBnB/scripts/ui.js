/* ==========================================================================
   ui.js — modal de importação, cards, chips, listas de filtro e toasts
   ========================================================================== */
(function (global) {
  'use strict';

  var $ = function (id) { return document.getElementById(id); };

  function esc(s) {
    return String(s === null || s === undefined ? '' : s)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  /* ============================== Toasts ============================== */

  function toast(message, kind, title) {
    var box = $('toasts');
    if (!box) return;
    var el = document.createElement('div');
    el.className = 'toast' + (kind ? ' toast--' + kind : '');
    el.innerHTML = '<div class="toast__body">' +
      (title ? '<div class="toast__title">' + esc(title) + '</div>' : '') +
      '<div>' + message + '</div></div>';
    box.appendChild(el);
    setTimeout(function () {
      el.style.transition = 'opacity .3s, transform .3s';
      el.style.opacity = '0';
      el.style.transform = 'translateX(20px)';
      setTimeout(function () { el.remove(); }, 320);
    }, kind === 'error' ? 8000 : 4600);
  }

  /* ========================= Modal de importação ====================== */

  var queue = [];          // { file, headers, map, missing, cityName, currency, fxRate, el }
  var importing = false;

  function openImport(mandatory) {
    var modal = $('importModal');
    modal.classList.remove('hidden');
    $('importClose').classList.toggle('hidden', !!mandatory);
    $('importCancel').classList.toggle('hidden', !!mandatory);
    modal.dataset.mandatory = mandatory ? '1' : '';
    if (!mandatory) $('importClose').focus();
  }

  function closeImport() {
    if ($('importModal').dataset.mandatory === '1' && !global.Store.state.datasets.length) {
      toast('Importe ao menos uma base de cidade para usar o dashboard.', 'warn');
      return;
    }
    $('importModal').classList.add('hidden');
    resetQueue();
  }

  function resetQueue() {
    queue = [];
    $('importList').innerHTML = '';
    $('importMapping').classList.add('hidden');
    $('mappingFields').innerHTML = '';
    $('importStatus').textContent = 'Nenhum arquivo selecionado.';
    $('importConfirm').disabled = true;
    $('fileInput').value = '';
  }

  function humanSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(0) + ' KB';
    return (bytes / 1024 / 1024).toFixed(1) + ' MB';
  }

  /** Adiciona arquivos à fila, lendo só o cabeçalho de cada um. */
  function addFiles(fileList) {
    var files = Array.prototype.slice.call(fileList).filter(function (f) {
      return /\.csv$/i.test(f.name) || f.type === 'text/csv';
    });
    if (!files.length) {
      toast('Selecione arquivos <b>.csv</b>.', 'warn');
      return;
    }

    files.forEach(function (file) {
      if (queue.some(function (q) { return q.file.name === file.name && q.file.size === file.size; })) return;

      var entry = {
        file: file,
        headers: null,
        map: null,
        missing: [],
        cityName: CsvLoader.cityFromFilename(file.name),
        currency: '',
        fxRate: 1,
        ready: false
      };
      queue.push(entry);
      renderQueue();

      CsvLoader.peekHeaders(file).then(function (headers) {
        entry.headers = headers;
        var det = CsvLoader.detectColumns(headers);
        entry.map = det.map;
        entry.missing = det.missing;
        entry.ready = det.missing.length === 0;
        renderQueue();
      }).catch(function (err) {
        entry.error = err.message || String(err);
        renderQueue();
      });
    });
  }

  function renderQueue() {
    var list = $('importList');
    list.innerHTML = '';

    queue.forEach(function (entry, i) {
      var div = document.createElement('div');
      div.className = 'import-item';

      var status = entry.error
        ? '<span style="color:var(--danger)">' + esc(entry.error) + '</span>'
        : entry.headers
          ? (entry.missing.length
              ? '<span class="badge badge--warn">mapear colunas</span>'
              : '<span class="badge badge--ok">' + entry.headers.length + ' colunas reconhecidas</span>')
          : '<span class="faint">lendo cabeçalho…</span>';

      div.innerHTML =
        '<div class="import-item__top">' +
          '<span>📄</span>' +
          '<span class="import-item__name">' + esc(entry.file.name) + '</span>' +
          '<span class="import-item__size">' + humanSize(entry.file.size) + '</span>' +
          '<span class="spacer" style="flex:1"></span>' +
          status +
          '<button class="chip__x" data-rm="' + i + '" title="Remover da lista">×</button>' +
        '</div>' +
        '<div class="import-item__fields">' +
          '<div class="field"><label class="field__label">Nome da cidade</label>' +
            '<input class="input input--xs" data-city="' + i + '" value="' + esc(entry.cityName) + '"></div>' +
          '<div class="field"><label class="field__label">Moeda</label>' +
            '<input class="input input--xs" data-cur="' + i + '" list="curList" placeholder="ex.: BRL" value="' + esc(entry.currency) + '"></div>' +
          '<div class="field"><label class="field__label">Taxa p/ moeda comum</label>' +
            '<input class="input input--xs" data-fx="' + i + '" type="number" min="0.0001" step="0.0001" value="' + entry.fxRate + '"></div>' +
        '</div>' +
        '<div class="progress" data-prog="' + i + '"><i></i></div>';

      list.appendChild(div);
    });

    if (!$('curList')) {
      var dl = document.createElement('datalist');
      dl.id = 'curList';
      ['BRL', 'USD', 'EUR', 'GBP', 'ARS', 'CLP', 'MXN', 'JPY'].forEach(function (c) {
        var o = document.createElement('option');
        o.value = c;
        dl.appendChild(o);
      });
      document.body.appendChild(dl);
    }

    renderMapping();

    var pending = queue.filter(function (e) { return !e.error; });
    $('importStatus').textContent = pending.length
      ? pending.length + ' arquivo(s) na fila'
      : 'Nenhum arquivo selecionado.';
    $('importConfirm').disabled = importing || !pending.length ||
      pending.some(function (e) { return !e.headers || e.missing.length; });
  }

  /** Tela de mapeamento manual — só aparece quando a detecção falha. */
  function renderMapping() {
    var needs = queue.filter(function (e) { return e.headers && e.missing.length; });
    var box = $('importMapping');
    var fields = $('mappingFields');

    if (!needs.length) { box.classList.add('hidden'); fields.innerHTML = ''; return; }
    box.classList.remove('hidden');

    var html = '';
    needs.forEach(function (entry) {
      var qi = queue.indexOf(entry);
      html += '<div style="grid-column:1/-1" class="h-section">' + esc(entry.file.name) + '</div>';
      CsvLoader.ROLES.forEach(function (role) {
        var opts = '<option value="">— nenhuma —</option>';
        entry.headers.forEach(function (h) {
          opts += '<option value="' + esc(h) + '"' +
                  (entry.map[role.key] === h ? ' selected' : '') + '>' + esc(h) + '</option>';
        });
        html += '<div class="field"><label class="field__label">' + role.label +
                (role.required ? ' <span style="color:var(--danger)">*</span>' : '') + '</label>' +
                '<select class="select" data-role="' + role.key + '" data-entry="' + qi + '">' + opts + '</select></div>';
      });
    });
    fields.innerHTML = html;
  }

  /** Executa a importação de todos os itens da fila. */
  function runImport() {
    if (importing) return;
    var items = queue.filter(function (e) { return !e.error && e.headers && !e.missing.length; });
    if (!items.length) return;

    importing = true;
    $('importConfirm').disabled = true;
    $('importConfirm').innerHTML = '<span class="spin"></span> Importando…';

    var done = 0, failed = 0;

    function step(i) {
      if (i >= items.length) {
        importing = false;
        $('importConfirm').innerHTML = 'Importar bases';
        $('importModal').dataset.mandatory = '';
        $('importModal').classList.add('hidden');
        resetQueue();
        if (done) {
          toast(done + ' base(s) importada(s) com sucesso.', 'ok', 'Importação concluída');
          global.App.onDataChanged(true);
        }
        if (failed) toast(failed + ' arquivo(s) não puderam ser importados.', 'error');
        return;
      }

      var entry = items[i];
      var bar = document.querySelector('[data-prog="' + queue.indexOf(entry) + '"] i');

      CsvLoader.loadFile(entry.file, {
        cityName: entry.cityName,
        currency: entry.currency,
        fxRate: entry.fxRate,
        columnMap: entry.map
      }, function (p) {
        if (bar) bar.style.width = (p * 100).toFixed(1) + '%';
      }).then(function (ds) {
        global.Store.addDataset(ds);
        done++;
        var msg = ds.cityName + ': ' + Stats.fmtNum(ds.count) + ' anúncios';
        if (ds.noPriceCount) {
          msg += ' · <b>' + Stats.fmtNum(ds.noPriceCount) + '</b> sem preço (' +
                 Stats.fmtPct(ds.noPriceCount / ds.count) + ')';
        }
        if (ds.droppedCoords) msg += ' · ' + Stats.fmtNum(ds.droppedCoords) + ' descartados por coordenada inválida';
        toast(msg, 'ok');
        step(i + 1);
      }).catch(function (err) {
        failed++;
        toast(esc(entry.file.name) + ': ' + esc(err.message || err), 'error', 'Falha na importação');
        step(i + 1);
      });
    }

    step(0);
  }

  /* =========================== Chips de cidade ======================== */

  function renderCityChips() {
    var box = $('cityChips');
    if (!box) return;
    var ds = global.Store.state.datasets;

    if (!ds.length) { box.innerHTML = ''; return; }

    box.innerHTML = ds.map(function (d) {
      return '<span class="city-chip' + (d.visible ? '' : ' is-off') + '" data-city="' + d.id + '" ' +
             'title="' + esc(d.sourceFile) + ' · clique para mostrar/ocultar">' +
             '<i class="city-chip__dot" style="background:' + d.color + '"></i>' +
             esc(d.cityName) +
             '<span class="city-chip__n">' + Stats.fmtNum(d.count) + '</span>' +
             '<button class="city-chip__x" data-remove="' + d.id + '" title="Remover base">×</button>' +
             '</span>';
    }).join('');
  }

  /* ============================== Cards =============================== */

  /** Qual conjunto de preços os cards devem mostrar e em que moeda. */
  function moneyContext(result, opts) {
    if (result.cities.length === 1) {
      return { stats: result.cities[0].stats, currency: result.cities[0].ds.currency, converted: false };
    }
    return { stats: result.commonStats, currency: opts.commonCurrency, converted: true };
  }

  function card(label, value, hint, color) {
    return '<div class="stat-card" style="--accent-color:' + (color || 'var(--brand2)') + '">' +
      '<div class="stat-card__label">' + label + '</div>' +
      '<div class="stat-card__value">' + value + '</div>' +
      '<div class="stat-card__hint">' + (hint || '') + '</div></div>';
  }

  function renderCards(result, opts) {
    var box = $('cards');
    if (!box) return;

    if (!result || !result.cities.length) {
      box.innerHTML = '<div class="stat-card" style="grid-column:1/-1">' +
        '<div class="stat-card__label">Sem dados</div>' +
        '<div class="stat-card__value">—</div>' +
        '<div class="stat-card__hint">Importe uma base de cidade para começar.</div></div>';
      return;
    }

    var mc = moneyContext(result, opts);
    var s = mc.stats;
    var cur = mc.currency;
    var totalImported = global.Store.activeDatasets().reduce(function (a, d) { return a + d.count; }, 0);
    var money = function (v) { return Stats.fmtMoney(v, cur); };

    var avgAvail = 0, availW = 0, occ = 0, occW = 0, nights = 0, nightsW = 0, revs = 0, revsW = 0, nbSet = new Set();
    result.cities.forEach(function (c) {
      if (isFinite(c.avgAvailability)) { avgAvail += c.avgAvailability * c.count; availW += c.count; }
      if (isFinite(c.occupancy)) { occ += c.occupancy * c.count; occW += c.count; }
      if (isFinite(c.avgMinNights)) { nights += c.avgMinNights * c.count; nightsW += c.count; }
      if (isFinite(c.avgReviews)) { revs += c.avgReviews * c.count; revsW += c.count; }
      c.byNeighbourhood.forEach(function (_, k) { nbSet.add(c.ds.cityName + '|' + k); });
    });

    var outliersTotal = global.Store.activeDatasets().reduce(function (a, d) {
      return a + (d.outlierInfo ? d.outlierInfo.count : 0);
    }, 0);

    var convNote = mc.converted ? ' · convertido p/ ' + esc(cur) : '';
    var html = '';

    html += card('Anúncios no filtro', Stats.fmtNum(result.total),
      'de ' + Stats.fmtNum(totalImported) + ' importados (' + Stats.fmtPct(totalImported ? result.total / totalImported : 0) + ')',
      'var(--brand)');

    html += card('Preço médio', money(s.mean), 'sobre ' + Stats.fmtNum(s.count) + ' com preço' + convNote, 'var(--brand2)');
    html += card('Mediana', money(s.median), 'metade dos anúncios abaixo disso', 'var(--info)');
    html += card('Mínimo', money(s.min), 'menor preço no filtro', 'var(--ok)');
    html += card('Máximo', money(s.max), 'maior preço no filtro', 'var(--danger)');
    html += card('Desvio padrão', money(s.stdDev), 'CV = ' + Stats.fmtNum(s.cv, 2), 'var(--accent)');
    html += card('Miolo do mercado (Q1–Q3)', money(s.q1) + ' – ' + money(s.q3),
      'IQR ' + money(s.iqr), 'var(--info)');
    html += card('P5 – P95', money(s.p05) + ' – ' + money(s.p95), 'faixa dos 90% centrais', 'var(--brand2)');

    html += card('Outliers tratados', Stats.fmtNum(outliersTotal),
      (Outliers.LABELS[opts.outlierMethod] || '—') + ' · ' + (Outliers.ACTION_LABELS[opts.outlierAction] || ''),
      'var(--warn)');

    html += card('Bairros com oferta', Stats.fmtNum(nbSet.size),
      result.cities.length + ' cidade(s) no filtro', 'var(--brand)');

    html += card('Ocupação estimada', Stats.fmtPct(occW ? occ / occW : NaN),
      'disponibilidade média ' + Stats.fmtNum(availW ? avgAvail / availW : NaN, 0) + ' dias/ano', 'var(--ok)');

    html += card('Noites mínimas (média)', Stats.fmtNum(nightsW ? nights / nightsW : NaN, 1),
      'avaliações por anúncio: ' + Stats.fmtNum(revsW ? revs / revsW : NaN, 1), 'var(--info)');

    box.innerHTML = html;
  }

  /* ======================= Listas de filtro ========================== */

  function aggregate(getList, getCounts) {
    var m = new Map();
    global.Store.activeDatasets().forEach(function (ds) {
      getList(ds).forEach(function (k) {
        m.set(k, (m.get(k) || 0) + (getCounts(ds).get(k) || 0));
      });
    });
    return m;
  }

  function renderOptionList(el, entries, selected, name, colorFn) {
    if (!entries.length) {
      el.innerHTML = '<div class="faint" style="padding:8px;font-size:12px">Sem valores nesta base.</div>';
      return;
    }
    var sel = new Set(selected);
    el.innerHTML = entries.map(function (e) {
      return '<label class="check">' +
        '<input type="checkbox" data-filter="' + name + '" value="' + esc(e[0]) + '"' +
        (sel.has(e[0]) ? ' checked' : '') + '>' +
        (colorFn ? '<i class="check__dot" style="background:' + colorFn(e[0]) + '"></i>' : '') +
        '<span class="check__text" title="' + esc(e[0]) + '">' + esc(e[0]) + '</span>' +
        '<span class="check__count">' + Stats.fmtNum(e[1]) + '</span></label>';
    }).join('');
  }

  function renderFilterLists() {
    var f = global.Store.state.filters;
    var active = global.Store.activeDatasets();

    // Tipos de acomodação
    var rt = aggregate(function (d) { return d.roomTypes; }, function (d) { return d.roomTypeCounts; });
    var rtEntries = Array.from(rt.entries()).sort(function (a, b) { return b[1] - a[1]; });
    var rtIndex = {};
    rtEntries.forEach(function (e, i) { rtIndex[e[0]] = i; });
    renderOptionList($('fRoomTypes'), rtEntries, f.roomTypes, 'roomTypes', function (k) {
      return MapView.CAT_COLORS[rtIndex[k] % MapView.CAT_COLORS.length];
    });

    // Regiões / boroughs — só existem em bases que trazem a coluna preenchida
    var gp = aggregate(function (d) { return d.groups; }, function (d) { return d.groupCounts; });
    var gpEntries = Array.from(gp.entries()).sort(function (a, b) { return b[1] - a[1]; });
    $('groupBox').classList.toggle('hidden', gpEntries.length === 0);
    if (gpEntries.length) renderOptionList($('fGroups'), gpEntries, f.groups, 'groups');

    // Bairros (com busca)
    var nb = aggregate(function (d) { return d.neighbourhoods; }, function (d) { return d.neighbourhoodCounts; });
    var needle = Stats.normalizeText($('fNbSearch').value);
    var nbEntries = Array.from(nb.entries())
      .filter(function (e) { return !needle || Stats.normalizeText(e[0]).indexOf(needle) >= 0; })
      .sort(function (a, b) { return b[1] - a[1]; });
    renderOptionList($('fNeighbourhoods'), nbEntries.slice(0, 400), f.neighbourhoods, 'neighbourhoods');

    // Contador de anúncios sem preço
    var noPrice = active.reduce(function (a, d) { return a + d.noPriceCount; }, 0);
    $('noPriceCount').textContent = noPrice ? Stats.fmtNum(noPrice) : '';
  }

  /* ==================== Chips de filtros ativos ====================== */

  function describeFilters(opts) {
    var f = global.Store.state.filters;
    var parts = [];
    if (f.search) parts.push('busca "' + f.search + '"');
    if (f.priceMin !== null || f.priceMax !== null) {
      parts.push('preço ' + Stats.fmtMoney(f.priceMin, opts.commonCurrency) +
                 ' a ' + Stats.fmtMoney(f.priceMax, opts.commonCurrency));
    }
    if (f.roomTypes.length) parts.push('tipo: ' + f.roomTypes.join(', '));
    if (f.groups.length) parts.push('região: ' + f.groups.join(', '));
    if (f.neighbourhoods.length) parts.push(f.neighbourhoods.length + ' bairro(s)');
    if (f.minNightsMax !== null) parts.push('até ' + f.minNightsMax + ' noites mínimas');
    if (f.availMin) parts.push('disponibilidade ≥ ' + f.availMin + ' dias');
    if (f.reviewsMin) parts.push('≥ ' + f.reviewsMin + ' avaliações');
    if (f.includeNoPrice) parts.push('incluindo anúncios sem preço');
    var hidden = global.Store.state.datasets.filter(function (d) { return !d.visible; });
    if (hidden.length) parts.push('ocultas: ' + hidden.map(function (d) { return d.cityName; }).join(', '));
    return parts.length ? parts.join(' · ') : 'nenhum';
  }

  function renderActiveFilters(result, opts) {
    var box = $('activeFilters');
    if (!box) return;
    var f = global.Store.state.filters;
    var chips = [];

    if (f.search) chips.push(['search', 'busca: ' + esc(f.search)]);
    if (f.roomTypes.length) chips.push(['roomTypes', f.roomTypes.length + ' tipo(s)']);
    if (f.groups.length) chips.push(['groups', f.groups.length + ' região(ões)']);
    if (f.neighbourhoods.length) chips.push(['neighbourhoods', f.neighbourhoods.length + ' bairro(s)']);
    if (f.minNightsMax !== null) chips.push(['minNightsMax', '≤ ' + f.minNightsMax + ' noites mín.']);
    if (f.availMin) chips.push(['availMin', 'disp. ≥ ' + f.availMin + 'd']);
    if (f.reviewsMin) chips.push(['reviewsMin', '≥ ' + f.reviewsMin + ' avaliações']);

    var html = chips.map(function (c) {
      return '<span class="chip chip--active">' + c[1] +
             '<button class="chip__x" data-clear="' + c[0] + '" title="Remover filtro">×</button></span>';
    }).join('');

    if (result && result.outliersInView && opts.outlierAction === 'highlight') {
      html += '<span class="chip chip--warn">⚠ ' + Stats.fmtNum(result.outliersInView) +
              ' outliers destacados no mapa</span>';
    }
    if (result && result.multiCurrency && !opts.compareInCommon) {
      html += '<span class="chip chip--warn">⚠ moedas diferentes — ative a conversão no painel de comparação para ver os valores lado a lado</span>';
    }

    box.innerHTML = html;
  }

  /* ======================= Resumo de outliers ======================== */

  function renderOutlierInfo(opts) {
    var badge = $('outlierBadge');
    var detail = $('outlierDetail');
    var active = global.Store.activeDatasets();

    if (!active.length || opts.outlierMethod === 'none') {
      badge.textContent = opts.outlierMethod === 'none' ? 'desligado' : '—';
      badge.className = 'badge';
      detail.innerHTML = '';
      return;
    }

    var total = active.reduce(function (a, d) { return a + (d.outlierInfo ? d.outlierInfo.count : 0); }, 0);
    var n = active.reduce(function (a, d) { return a + d.count; }, 0);
    badge.textContent = Stats.fmtNum(total) + ' (' + Stats.fmtPct(n ? total / n : 0) + ')';
    badge.className = 'badge badge--warn';

    detail.innerHTML = active.map(function (d) {
      if (!d.outlierInfo || !d.outlierInfo.groups.length) return '';
      var g = d.outlierInfo.groups[0];
      var limits = (d.outlierInfo.groups.length > 1)
        ? 'limites por tipo de acomodação'
        : 'limites ' + Stats.fmtMoney(Math.max(0, g.low), d.currency) + ' – ' + Stats.fmtMoney(g.high, d.currency);
      return '<div><span style="color:' + d.color + '">●</span> ' + esc(d.cityName) + ': ' +
             Stats.fmtNum(d.outlierInfo.count) + ' · ' + limits + '</div>';
    }).join('');
  }

  /* ============================ Exportação =========================== */

  function renderExportSummary(result, opts) {
    var box = $('exportSummaryBox');
    if (!box) return;
    if (!result || !result.cities.length) {
      box.innerHTML = 'Nenhum dado carregado.';
      return;
    }
    box.innerHTML = '<b>' + Stats.fmtNum(result.total) + '</b> anúncios em <b>' +
      result.cities.length + '</b> cidade(s) — ' +
      result.cities.map(function (c) { return esc(c.ds.cityName) + ' (' + Stats.fmtNum(c.count) + ')'; }).join(', ') +
      '.<br>Filtros: ' + esc(describeFilters(opts));
  }

  global.UI = {
    $: $,
    esc: esc,
    toast: toast,
    openImport: openImport,
    closeImport: closeImport,
    addFiles: addFiles,
    renderQueue: renderQueue,
    runImport: runImport,
    getQueue: function () { return queue; },
    renderCityChips: renderCityChips,
    renderCards: renderCards,
    renderFilterLists: renderFilterLists,
    renderActiveFilters: renderActiveFilters,
    renderOutlierInfo: renderOutlierInfo,
    renderExportSummary: renderExportSummary,
    describeFilters: describeFilters,
    moneyContext: moneyContext
  };
})(window);
