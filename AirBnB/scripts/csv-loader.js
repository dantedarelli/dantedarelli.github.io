/* ==========================================================================
   csv-loader.js — importação genérica de CSV (PapaParse)
   Nada de cidade, bairro, moeda ou coluna fica fixo no código: os papéis são
   descobertos no cabeçalho do arquivo ou escolhidos pelo usuário na UI.
   ========================================================================== */
(function (global) {
  'use strict';

  var norm = Stats.normalizeText;

  /**
   * Papéis lógicos usados pelo dashboard e os sinônimos aceitos no cabeçalho.
   * A busca é feita sem acento, em minúsculas e ignorando separadores.
   */
  var ROLES = [
    { key: 'latitude',  label: 'Latitude',        required: true,  numeric: true,
      aliases: ['latitude', 'lat', 'y'] },
    { key: 'longitude', label: 'Longitude',       required: true,  numeric: true,
      aliases: ['longitude', 'long', 'lon', 'lng', 'x'] },
    { key: 'price',     label: 'Preço',           required: true,  numeric: true,
      aliases: ['price', 'preco', 'valor', 'diaria', 'daily rate', 'rate'] },
    { key: 'neighbourhood', label: 'Bairro',      required: false,
      aliases: ['neighbourhood', 'neighborhood', 'bairro', 'district', 'zona', 'area'] },
    { key: 'group',     label: 'Região / Borough', required: false,
      aliases: ['neighbourhood group', 'neighborhood group', 'borough', 'regiao', 'region', 'municipio', 'zona administrativa'] },
    { key: 'roomType',  label: 'Tipo de acomodação', required: false,
      aliases: ['room type', 'roomtype', 'tipo', 'tipo de quarto', 'tipo de acomodacao', 'property type'] },
    { key: 'name',      label: 'Nome do anúncio', required: false,
      aliases: ['name', 'nome', 'title', 'titulo', 'listing name'] },
    { key: 'id',        label: 'ID do anúncio',   required: false,
      aliases: ['id', 'listing id', 'codigo'] },
    { key: 'hostName',  label: 'Anfitrião',       required: false,
      aliases: ['host name', 'hostname', 'anfitriao', 'host'] },
    { key: 'minNights', label: 'Noites mínimas',  required: false, numeric: true,
      aliases: ['minimum nights', 'min nights', 'noites minimas', 'minimo de noites'] },
    { key: 'reviews',   label: 'Nº de avaliações', required: false, numeric: true,
      aliases: ['number of reviews', 'reviews', 'avaliacoes', 'num reviews'] },
    { key: 'reviewsPerMonth', label: 'Avaliações/mês', required: false, numeric: true,
      aliases: ['reviews per month', 'avaliacoes por mes', 'reviews mes'] },
    { key: 'lastReview', label: 'Última avaliação', required: false,
      aliases: ['last review', 'ultima avaliacao', 'ultima review'] },
    { key: 'availability', label: 'Disponibilidade (dias/ano)', required: false, numeric: true,
      aliases: ['availability 365', 'availability', 'disponibilidade', 'dias disponiveis'] },
    { key: 'hostListings', label: 'Anúncios do anfitrião', required: false, numeric: true,
      aliases: ['calculated host listings count', 'host listings count', 'anuncios do anfitriao'] }
  ];

  /** "neighbourhood_group" e "Neighbourhood Group" viram a mesma chave. */
  function canonical(header) {
    return norm(header).replace(/[_\-.]+/g, ' ').replace(/\s+/g, ' ').trim();
  }

  /**
   * Tenta casar cada papel com uma coluna real do arquivo.
   * @param {string[]} headers
   * @returns {{map:Object, missing:string[]}}
   */
  function detectColumns(headers) {
    var canon = headers.map(canonical);
    var map = {};
    var used = {};

    ROLES.forEach(function (role) {
      // 1ª passada: igualdade exata com algum sinônimo
      for (var a = 0; a < role.aliases.length; a++) {
        var idx = canon.indexOf(role.aliases[a]);
        if (idx >= 0 && !used[headers[idx]]) {
          map[role.key] = headers[idx];
          used[headers[idx]] = true;
          return;
        }
      }
      // 2ª passada: o cabeçalho começa com o sinônimo (ex.: "price_usd")
      for (var b = 0; b < role.aliases.length; b++) {
        for (var i = 0; i < canon.length; i++) {
          if (!used[headers[i]] && canon[i].indexOf(role.aliases[b]) === 0) {
            map[role.key] = headers[i];
            used[headers[i]] = true;
            return;
          }
        }
      }
    });

    var missing = ROLES.filter(function (r) { return r.required && !map[r.key]; })
                       .map(function (r) { return r.key; });
    return { map: map, missing: missing };
  }

  /** Aceita "1.234,50", "R$ 1234.50", "$1,234.50" e devolve número ou null. */
  function parseNumber(raw) {
    if (raw === null || raw === undefined) return null;
    if (typeof raw === 'number') return isFinite(raw) ? raw : null;
    var s = String(raw).trim();
    if (!s) return null;

    s = s.replace(/[^\d,.\-]/g, '');       // remove R$, US$, espaços, %
    if (!s || s === '-' || s === '.' || s === ',') return null;

    var lastComma = s.lastIndexOf(',');
    var lastDot = s.lastIndexOf('.');
    if (lastComma >= 0 && lastDot >= 0) {
      // o separador decimal é o que aparece por último
      if (lastComma > lastDot) s = s.replace(/\./g, '').replace(',', '.');
      else s = s.replace(/,/g, '');
    } else if (lastComma >= 0) {
      // vírgula sozinha: 3 dígitos depois = separador de milhar; senão, decimal
      var after = s.length - lastComma - 1;
      s = (after === 3) ? s.replace(/,/g, '') : s.replace(',', '.');
    }
    var n = parseFloat(s);
    return isFinite(n) ? n : null;
  }

  /** "ny.csv" → "NY"; "rio_de_janeiro.csv" → "Rio De Janeiro" */
  function cityFromFilename(filename) {
    var base = String(filename || '').replace(/\.[^.]+$/, '').replace(/[_\-]+/g, ' ').trim();
    if (!base) return 'Cidade';
    if (base.length <= 3) return base.toUpperCase();
    return base.replace(/\b\w/g, function (c) { return c.toUpperCase(); });
  }

  /** Lê só o cabeçalho (rápido) para montar a tela de mapeamento. */
  function peekHeaders(file) {
    return new Promise(function (resolve, reject) {
      Papa.parse(file, {
        header: true,
        preview: 1,
        skipEmptyLines: 'greedy',
        encoding: 'utf-8',
        complete: function (res) {
          var fields = (res.meta && res.meta.fields) ? res.meta.fields.filter(Boolean) : [];
          if (!fields.length) reject(new Error('Não foi possível ler o cabeçalho do arquivo.'));
          else resolve(fields);
        },
        error: function (err) { reject(err); }
      });
    });
  }

  /**
   * Importa um arquivo CSV e devolve um dataset pronto para o Store.
   * @param {File} file
   * @param {object} meta - { cityName, currency, fxRate, columnMap }
   * @param {function(number)} onProgress - 0..1
   */
  function loadFile(file, meta, onProgress) {
    return new Promise(function (resolve, reject) {
      var rows = [];
      var columnMap = meta.columnMap || null;
      var headers = null;
      var dropped = { coords: 0, empty: 0 };
      var total = file.size || 1;

      function col(row, key) {
        var name = columnMap && columnMap[key];
        return name ? row[name] : undefined;
      }

      Papa.parse(file, {
        header: true,
        skipEmptyLines: 'greedy',
        encoding: 'utf-8',
        worker: false,           // o step síncrono já mantém a UI responsiva o bastante
        chunkSize: 1024 * 512,
        chunk: function (results, parser) {
          if (!headers) {
            headers = (results.meta && results.meta.fields) ? results.meta.fields.filter(Boolean) : [];
            if (!columnMap) columnMap = detectColumns(headers).map;
            var miss = ROLES.filter(function (r) { return r.required && !columnMap[r.key]; });
            if (miss.length) {
              parser.abort();
              reject(new Error('Colunas obrigatórias não encontradas: ' +
                miss.map(function (r) { return r.label; }).join(', ')));
              return;
            }
          }

          var data = results.data;
          for (var i = 0; i < data.length; i++) {
            var r = data[i];
            var lat = parseNumber(col(r, 'latitude'));
            var lon = parseNumber(col(r, 'longitude'));

            if (lat === null || lon === null ||
                lat < -90 || lat > 90 || lon < -180 || lon > 180 ||
                (lat === 0 && lon === 0)) {
              dropped.coords++;
              continue;
            }

            var price = parseNumber(col(r, 'price'));
            if (price !== null && price < 0) price = null;

            rows.push({
              id: col(r, 'id') || '',
              name: (col(r, 'name') || '').trim(),
              hostName: (col(r, 'hostName') || '').trim(),
              lat: lat,
              lon: lon,
              price: price,
              neighbourhood: (col(r, 'neighbourhood') || '').trim(),
              group: (col(r, 'group') || '').trim(),
              roomType: (col(r, 'roomType') || '').trim(),
              minNights: parseNumber(col(r, 'minNights')),
              reviews: parseNumber(col(r, 'reviews')),
              reviewsPerMonth: parseNumber(col(r, 'reviewsPerMonth')),
              lastReview: (col(r, 'lastReview') || '').trim(),
              availability: parseNumber(col(r, 'availability')),
              hostListings: parseNumber(col(r, 'hostListings'))
            });
          }

          if (onProgress) {
            var cursor = (results.meta && results.meta.cursor) || 0;
            onProgress(Math.min(0.98, cursor / total));
          }
        },
        complete: function () {
          if (!rows.length) {
            reject(new Error('Nenhuma linha válida encontrada em ' + file.name + '.'));
            return;
          }
          if (onProgress) onProgress(1);
          resolve(buildDataset(rows, file, meta, columnMap, headers, dropped));
        },
        error: function (err) { reject(err); }
      });
    });
  }

  /** Monta o objeto de dataset, com colunas tipadas para o mapa. */
  function buildDataset(rows, file, meta, columnMap, headers, dropped) {
    var n = rows.length;
    var lat = new Float32Array(n);
    var lon = new Float32Array(n);
    var price = new Float64Array(n);
    var priceValid = new Uint8Array(n);

    var neighbourhoods = new Map();
    var groups = new Map();
    var roomTypes = new Map();
    var noPrice = 0;

    for (var i = 0; i < n; i++) {
      var r = rows[i];
      lat[i] = r.lat;
      lon[i] = r.lon;
      if (r.price !== null && isFinite(r.price) && r.price > 0) {
        price[i] = r.price;
        priceValid[i] = 1;
      } else {
        price[i] = NaN;
        noPrice++;
      }
      if (r.neighbourhood) neighbourhoods.set(r.neighbourhood, (neighbourhoods.get(r.neighbourhood) || 0) + 1);
      if (r.group) groups.set(r.group, (groups.get(r.group) || 0) + 1);
      if (r.roomType) roomTypes.set(r.roomType, (roomTypes.get(r.roomType) || 0) + 1);
    }

    function sortedKeys(m) {
      return Array.from(m.keys()).sort(function (a, b) { return a.localeCompare(b, 'pt-BR'); });
    }

    var bounds = { minLat: Infinity, maxLat: -Infinity, minLon: Infinity, maxLon: -Infinity };
    for (var j = 0; j < n; j++) {
      if (lat[j] < bounds.minLat) bounds.minLat = lat[j];
      if (lat[j] > bounds.maxLat) bounds.maxLat = lat[j];
      if (lon[j] < bounds.minLon) bounds.minLon = lon[j];
      if (lon[j] > bounds.maxLon) bounds.maxLon = lon[j];
    }

    return {
      id: null,                       // atribuído pelo Store
      cityName: meta.cityName || cityFromFilename(file.name),
      currency: meta.currency || '',
      fxRate: (typeof meta.fxRate === 'number' && meta.fxRate > 0) ? meta.fxRate : 1,
      sourceFile: file.name,
      sourceSize: file.size,
      importedAt: new Date(),
      columnMap: columnMap,
      headers: headers,
      rows: rows,
      lat: lat,
      lon: lon,
      price: price,
      priceValid: priceValid,
      outlierMask: new Uint8Array(n),  // preenchido por outliers.js
      priceAdj: Float64Array.from(price), // preço após winsorização (ver outliers.js)
      count: n,
      noPriceCount: noPrice,
      droppedCoords: dropped.coords,
      neighbourhoods: sortedKeys(neighbourhoods),
      neighbourhoodCounts: neighbourhoods,
      groups: sortedKeys(groups),
      groupCounts: groups,
      roomTypes: sortedKeys(roomTypes),
      roomTypeCounts: roomTypes,
      bounds: bounds,
      outlierInfo: null
    };
  }

  global.CsvLoader = {
    ROLES: ROLES,
    detectColumns: detectColumns,
    parseNumber: parseNumber,
    cityFromFilename: cityFromFilename,
    peekHeaders: peekHeaders,
    loadFile: loadFile,
    canonical: canonical
  };
})(window);
