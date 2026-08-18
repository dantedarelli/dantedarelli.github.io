package br.com.viajarelegal.airbnb.domain

import br.com.viajarelegal.airbnb.data.Listing

/**
 * Estado dos filtros. Conjuntos vazios significam "todos" — assim uma cidade
 * recém-importada entra automaticamente na análise sem precisar ser marcada.
 */
data class FiltroState(
    val busca: String = "",
    val cidades: Set<String> = emptySet(),
    val tipos: Set<String> = emptySet(),
    val bairros: Set<String> = emptySet(),
    val precoMinComum: Double? = null,
    val precoMaxComum: Double? = null,
    val noitesMaximas: Int? = null,
    val disponibilidadeMinima: Int = 0,
    val avaliacoesMinimas: Int = 0,
) {
    val ativos: List<String>
        get() = buildList {
            if (busca.isNotBlank()) add("Busca: \"$busca\"")
            if (cidades.isNotEmpty()) add("Cidades: ${cidades.sorted().joinToString(", ")}")
            if (tipos.isNotEmpty()) add("Tipos: ${tipos.size} selecionado(s)")
            if (bairros.isNotEmpty()) add("Bairros: ${bairros.size} selecionado(s)")
            if (precoMinComum != null || precoMaxComum != null) add("Faixa de preço")
            if (noitesMaximas != null) add("Até $noitesMaximas noite(s) mínima(s)")
            if (disponibilidadeMinima > 0) add("Disponível ≥ $disponibilidadeMinima dias")
            if (avaliacoesMinimas > 0) add("≥ $avaliacoesMinimas avaliações")
        }

    val vazio: Boolean get() = ativos.isEmpty()
}

/** Opções disponíveis para montar os controles, derivadas da base carregada. */
data class Facetas(
    val cidades: List<String>,
    val tipos: List<String>,
    val bairros: List<String>,
    val precoMinComum: Double,
    val precoMaxComum: Double,
)

object Filters {

    fun facetas(base: List<Listing>, taxas: Map<String, Double>): Facetas {
        if (base.isEmpty()) return Facetas(emptyList(), emptyList(), emptyList(), 0.0, 0.0)
        val precos = base.map { precoComum(it, taxas) }
        return Facetas(
            cidades = base.map { it.cidade }.distinct().sorted(),
            tipos = base.map { it.tipo }.distinct().sorted(),
            bairros = base.map { it.bairro }.distinct().sorted(),
            precoMinComum = precos.min(),
            precoMaxComum = precos.max(),
        )
    }

    /** Preço trazido à moeda comum (BRL) pela taxa vigente. */
    fun precoComum(l: Listing, taxas: Map<String, Double>): Double =
        l.preco * (taxas[l.moeda] ?: 1.0)

    fun aplicar(base: List<Listing>, f: FiltroState, taxas: Map<String, Double>): List<Listing> {
        val termo = f.busca.trim().lowercase()
        return base.filter { l ->
            if (f.cidades.isNotEmpty() && l.cidade !in f.cidades) return@filter false
            if (f.tipos.isNotEmpty() && l.tipo !in f.tipos) return@filter false
            if (f.bairros.isNotEmpty() && l.bairro !in f.bairros) return@filter false
            if (f.noitesMaximas != null && l.noitesMinimas > f.noitesMaximas) return@filter false
            if (l.disponibilidade365 < f.disponibilidadeMinima) return@filter false
            if (l.avaliacoes < f.avaliacoesMinimas) return@filter false

            val preco = precoComum(l, taxas)
            if (f.precoMinComum != null && preco < f.precoMinComum) return@filter false
            if (f.precoMaxComum != null && preco > f.precoMaxComum) return@filter false

            if (termo.isNotEmpty()) {
                val alvo = "${l.nome} ${l.bairro} ${l.anfitriao} ${l.regiao} ${l.tipo}".lowercase()
                if (!alvo.contains(termo)) return@filter false
            }
            true
        }
    }
}
