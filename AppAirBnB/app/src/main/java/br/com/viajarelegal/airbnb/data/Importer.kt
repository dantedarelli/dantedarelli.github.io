package br.com.viajarelegal.airbnb.data

/**
 * Ponte entre uma planilha qualquer e o modelo [Listing].
 *
 * O reconhecimento das colunas é por apelidos (inglês do Inside Airbnb e
 * português), e o que não for reconhecido pode ser apontado à mão na tela de
 * importação — é isso que permite trazer uma cidade nova com cabeçalho diferente.
 */
object Importer {

    enum class Campo(
        val rotulo: String,
        val obrigatorio: Boolean,
        val apelidos: List<String>,
    ) {
        PRECO("Preço", true, listOf("price", "preco", "preço", "valor", "diaria", "diária", "preco_diaria")),
        BAIRRO("Bairro", true, listOf("neighbourhood", "neighborhood", "bairro", "district", "zona")),
        NOME("Nome do anúncio", false, listOf("name", "nome", "titulo", "título", "listing_name")),
        CIDADE("Cidade", false, listOf("city", "cidade", "municipio", "município", "base")),
        MOEDA("Moeda", false, listOf("currency", "moeda", "curr")),
        REGIAO("Região / borough", false, listOf("neighbourhood_group", "neighborhood_group", "regiao", "região", "borough", "grupo")),
        TIPO("Tipo de acomodação", false, listOf("room_type", "tipo", "tipo_acomodacao", "property_type", "acomodacao")),
        ID("Identificador", false, listOf("id", "listing_id", "codigo", "código")),
        ANFITRIAO("Anfitrião", false, listOf("host_name", "anfitriao", "anfitrião", "host", "proprietario")),
        LATITUDE("Latitude", false, listOf("latitude", "lat")),
        LONGITUDE("Longitude", false, listOf("longitude", "lon", "lng", "long")),
        NOITES("Noites mínimas", false, listOf("minimum_nights", "noites_minimas", "noites", "min_nights")),
        AVALIACOES("Avaliações", false, listOf("number_of_reviews", "avaliacoes", "avaliações", "reviews", "num_reviews")),
        AVALIACOES_MES("Avaliações por mês", false, listOf("reviews_per_month", "avaliacoes_por_mes", "reviews_month")),
        ULTIMA_AVALIACAO("Última avaliação", false, listOf("last_review", "ultima_avaliacao", "última_avaliação", "data_avaliacao")),
        DISPONIBILIDADE("Disponibilidade (dias/ano)", false, listOf("availability_365", "disponibilidade", "disponibilidade_365", "availability")),
        ANUNCIOS_HOST("Anúncios do anfitrião", false, listOf("calculated_host_listings_count", "anuncios_do_anfitriao", "host_listings", "host_listings_count")),
    }

    data class Resultado(
        val listings: List<Listing>,
        val ignoradas: Int,
        val motivo: String?,
    )

    private fun normalizar(s: String): String =
        s.trim().lowercase()
            .replace(Regex("[\\s\\-]+"), "_")
            .replace(Regex("[^a-z0-9_ãáàâéêíóôõúüç]"), "")

    /**
     * Casa cada campo com a coluna mais provável do cabeçalho. O índice devolvido
     * é a posição da coluna; um campo ausente simplesmente não entra no mapa.
     *
     * São duas passadas: primeiro igualdade exata com algum apelido, só depois
     * prefixo. Sem essa ordem, "neighbourhood" abocanharia a coluna
     * "neighbourhood_group" e a região se perderia.
     */
    fun detectar(cabecalho: List<String>): Map<Campo, Int> {
        val colunas = cabecalho.map { normalizar(it) }
        val mapa = mutableMapOf<Campo, Int>()
        val usadas = mutableSetOf<Int>()

        fun passada(criterio: (String, Campo) -> Boolean) {
            for (campo in Campo.entries) {
                if (mapa.containsKey(campo)) continue
                val indice = colunas.indices.firstOrNull { i ->
                    i !in usadas && criterio(colunas[i], campo)
                } ?: continue
                mapa[campo] = indice
                usadas += indice
            }
        }

        passada { coluna, campo -> coluna in campo.apelidos }
        passada { coluna, campo -> campo.apelidos.any { coluna.startsWith("${it}_") } }
        return mapa
    }

    fun faltamObrigatorios(mapa: Map<Campo, Int>): List<Campo> =
        Campo.entries.filter { it.obrigatorio && (mapa[it] ?: -1) < 0 }

    /**
     * Converte as linhas em anúncios. Linhas sem preço válido são contadas em
     * [Resultado.ignoradas] em vez de derrubar a importação inteira.
     */
    fun construir(
        tabela: CsvParser.Tabela,
        mapa: Map<Campo, Int>,
        cidadePadrao: String,
        moedaPadrao: String,
        origem: Origem = Origem.PLANILHA,
    ): Resultado {
        val faltando = faltamObrigatorios(mapa)
        if (faltando.isNotEmpty()) {
            return Resultado(emptyList(), tabela.linhas.size, "Falta indicar: ${faltando.joinToString { it.rotulo }}.")
        }

        fun texto(linha: List<String>, campo: Campo): String {
            val i = mapa[campo] ?: -1
            return if (i in linha.indices) linha[i].trim() else ""
        }

        val saida = mutableListOf<Listing>()
        var ignoradas = 0

        tabela.linhas.forEachIndexed { indice, linha ->
            val preco = CsvParser.numero(texto(linha, Campo.PRECO))
            val bairro = texto(linha, Campo.BAIRRO)
            if (preco == null || preco <= 0.0 || bairro.isBlank()) {
                ignoradas++
                return@forEachIndexed
            }
            val cidade = texto(linha, Campo.CIDADE).ifBlank { cidadePadrao }
            saida += Listing(
                id = texto(linha, Campo.ID).ifBlank { "IMP-${indice + 1}" },
                cidade = cidade,
                moeda = texto(linha, Campo.MOEDA).ifBlank { moedaPadrao }.uppercase(),
                nome = texto(linha, Campo.NOME).ifBlank { "Anúncio sem título" },
                anfitriao = texto(linha, Campo.ANFITRIAO).ifBlank { "—" },
                regiao = texto(linha, Campo.REGIAO),
                bairro = bairro,
                tipo = texto(linha, Campo.TIPO).ifBlank { "Não informado" },
                latitude = CsvParser.numero(texto(linha, Campo.LATITUDE)) ?: 0.0,
                longitude = CsvParser.numero(texto(linha, Campo.LONGITUDE)) ?: 0.0,
                preco = preco,
                noitesMinimas = CsvParser.inteiro(texto(linha, Campo.NOITES)) ?: 1,
                avaliacoes = CsvParser.inteiro(texto(linha, Campo.AVALIACOES)) ?: 0,
                avaliacoesPorMes = CsvParser.numero(texto(linha, Campo.AVALIACOES_MES)) ?: 0.0,
                ultimaAvaliacao = texto(linha, Campo.ULTIMA_AVALIACAO),
                disponibilidade365 = (CsvParser.inteiro(texto(linha, Campo.DISPONIBILIDADE)) ?: 0)
                    .coerceIn(0, 365),
                anunciosDoAnfitriao = CsvParser.inteiro(texto(linha, Campo.ANUNCIOS_HOST)) ?: 1,
                origem = origem,
            )
        }

        return Resultado(
            listings = saida,
            ignoradas = ignoradas,
            motivo = if (saida.isEmpty()) "Nenhuma linha tinha preço e bairro válidos." else null,
        )
    }
}
