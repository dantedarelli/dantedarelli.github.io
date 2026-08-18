package br.com.viajarelegal.airbnb.data

/** De onde a linha veio — usado para marcar visualmente o que o usuário acrescentou. */
enum class Origem { EMBUTIDO, PLANILHA, MANUAL }

/**
 * Um anúncio. Espelha as colunas do padrão Inside Airbnb já normalizadas.
 *
 * [preco] está sempre na moeda do próprio anúncio ([moeda]); a comparação entre
 * cidades usa [precoComum], convertido pela taxa de câmbio vigente no repositório.
 */
data class Listing(
    val id: String,
    val cidade: String,
    val moeda: String,
    val nome: String,
    val anfitriao: String,
    val regiao: String,
    val bairro: String,
    val tipo: String,
    val latitude: Double,
    val longitude: Double,
    val preco: Double,
    val noitesMinimas: Int,
    val avaliacoes: Int,
    val avaliacoesPorMes: Double,
    val ultimaAvaliacao: String,
    val disponibilidade365: Int,
    val anunciosDoAnfitriao: Int,
    val origem: Origem = Origem.EMBUTIDO,
) {
    /** Região quando existe (só NY traz borough); senão cai para o bairro. */
    val regiaoOuBairro: String get() = regiao.ifBlank { bairro }

    /** Ocupação estimada: fração do ano que o anúncio não está disponível. */
    val ocupacaoEstimada: Double get() = ((365 - disponibilidade365).coerceAtLeast(0)) / 365.0

    /** Ano da última avaliação, quando a data está preenchida e é legível. */
    val anoUltimaAvaliacao: Int?
        get() = ultimaAvaliacao.take(4).toIntOrNull()?.takeIf { it in 2008..2100 }
}
