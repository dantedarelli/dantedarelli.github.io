package br.com.viajarelegal.airbnb.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.viajarelegal.airbnb.data.Listing
import br.com.viajarelegal.airbnb.data.ListingRepository
import br.com.viajarelegal.airbnb.domain.ConfigOutlier
import br.com.viajarelegal.airbnb.domain.Facetas
import br.com.viajarelegal.airbnb.domain.FiltroState
import br.com.viajarelegal.airbnb.domain.Filters
import br.com.viajarelegal.airbnb.domain.ListingTratado
import br.com.viajarelegal.airbnb.domain.Outliers
import br.com.viajarelegal.airbnb.domain.ResultadoOutliers
import br.com.viajarelegal.airbnb.domain.Resumo
import br.com.viajarelegal.airbnb.domain.Stats

/** Métrica exibida no mapa de calor de bairros. */
enum class MetricaBairro(val rotulo: String) {
    MEDIANA("Preço mediano"),
    MEDIA("Preço médio"),
    CONTAGEM("Nº de anúncios"),
    DISPONIBILIDADE("Disponibilidade"),
    AVALIACOES("Avaliações"),
}

enum class Ordenacao(val rotulo: String) {
    PRECO_DESC("Maior preço"),
    PRECO_ASC("Menor preço"),
    AVALIACOES("Mais avaliados"),
    DISPONIBILIDADE("Mais disponíveis"),
    BAIRRO("Bairro (A–Z)"),
}

/** Retrato de uma cidade sob os filtros correntes. */
data class CidadeResumo(
    val cidade: String,
    val moeda: String,
    val taxa: Double,
    val resumoLocal: Resumo,
    val resumoComum: Resumo,
    val bairrosDistintos: Int,
    val noitesMinimasMedia: Double,
    val disponibilidadeMedia: Double,
    val ocupacaoEstimada: Double,
    val avaliacoesMedia: Double,
    val extremosDetectados: Int,
)

data class FatiaBairro(val bairro: String, val cidade: String, val valor: Double, val n: Int)
data class FatiaTipo(val tipo: String, val n: Int, val medianaComum: Double)

class DashboardViewModel : ViewModel() {

    var filtro by mutableStateOf(FiltroState())
    var config by mutableStateOf(ConfigOutlier())
    var metricaBairro by mutableStateOf(MetricaBairro.MEDIANA)
    var ordenacao by mutableStateOf(Ordenacao.PRECO_DESC)

    /** Mensagem efêmera exibida como snackbar após importações e ajustes. */
    var aviso by mutableStateOf<String?>(null)

    private val base: List<Listing> get() = ListingRepository.listings
    val taxas: Map<String, Double> get() = ListingRepository.taxas

    val facetas: Facetas by derivedStateOf { Filters.facetas(base, taxas) }

    val filtrados: List<Listing> by derivedStateOf { Filters.aplicar(base, filtro, taxas) }

    val tratamento: ResultadoOutliers by derivedStateOf { Outliers.aplicar(filtrados, config) }

    /** Anúncios efetivamente analisados, já com o tratamento de extremos aplicado. */
    val itens: List<ListingTratado> get() = tratamento.itens

    val totalBase: Int get() = base.size
    val totalImportados: Int get() = base.count { it.origem != br.com.viajarelegal.airbnb.data.Origem.EMBUTIDO }

    private fun comum(item: ListingTratado): Double =
        item.preco * (taxas[item.listing.moeda] ?: 1.0)

    /** Preços na base comum (BRL) — é o que permite comparar RJ e NY. */
    val precosComuns: List<Double> by derivedStateOf { itens.map { comum(it) } }

    val resumoGeral: Resumo by derivedStateOf { Stats.resumir(precosComuns) }

    val porCidade: List<CidadeResumo> by derivedStateOf {
        val extremosPorCidade = tratamento.detectadosPorCidade
        itens.groupBy { it.listing.cidade }
            .toSortedMap()
            .map { (cidade, doGrupo) ->
                val moeda = doGrupo.first().listing.moeda
                val taxa = taxas[moeda] ?: 1.0
                CidadeResumo(
                    cidade = cidade,
                    moeda = moeda,
                    taxa = taxa,
                    resumoLocal = Stats.resumir(doGrupo.map { it.preco }),
                    resumoComum = Stats.resumir(doGrupo.map { it.preco * taxa }),
                    bairrosDistintos = doGrupo.map { it.listing.bairro }.distinct().size,
                    noitesMinimasMedia = doGrupo.map { it.listing.noitesMinimas.toDouble() }.average(),
                    disponibilidadeMedia = doGrupo.map { it.listing.disponibilidade365.toDouble() }.average(),
                    ocupacaoEstimada = doGrupo.map { it.listing.ocupacaoEstimada }.average(),
                    avaliacoesMedia = doGrupo.map { it.listing.avaliacoes.toDouble() }.average(),
                    extremosDetectados = extremosPorCidade[cidade] ?: 0,
                )
            }
    }

    val histograma: List<Stats.Bin> by derivedStateOf { Stats.histograma(precosComuns) }

    val porTipo: List<FatiaTipo> by derivedStateOf {
        itens.groupBy { it.listing.tipo }
            .map { (tipo, g) -> FatiaTipo(tipo, g.size, Stats.mediana(g.map { comum(it) })) }
            .sortedByDescending { it.n }
    }

    /** Top bairros por preço mediano, com piso de 5 anúncios para evitar ruído. */
    val topBairros: List<FatiaBairro> by derivedStateOf {
        itens.groupBy { it.listing.bairro to it.listing.cidade }
            .filterValues { it.size >= 5 }
            .map { (chave, g) -> FatiaBairro(chave.first, chave.second, Stats.mediana(g.map { comum(it) }), g.size) }
            .sortedByDescending { it.valor }
            .take(12)
    }

    /** Todos os bairros sob a métrica escolhida — alimenta o mapa de calor. */
    val bairrosPorMetrica: List<FatiaBairro> by derivedStateOf {
        itens.groupBy { it.listing.bairro to it.listing.cidade }
            .map { (chave, g) ->
                val valor = when (metricaBairro) {
                    MetricaBairro.MEDIANA -> Stats.mediana(g.map { comum(it) })
                    MetricaBairro.MEDIA -> g.map { comum(it) }.average()
                    MetricaBairro.CONTAGEM -> g.size.toDouble()
                    MetricaBairro.DISPONIBILIDADE -> g.map { it.listing.disponibilidade365.toDouble() }.average()
                    MetricaBairro.AVALIACOES -> g.map { it.listing.avaliacoes.toDouble() }.average()
                }
                FatiaBairro(chave.first, chave.second, valor, g.size)
            }
            .sortedByDescending { it.n }
            .take(40)
    }

    /** Anúncios ativos por ano da última avaliação. */
    val linhaDoTempo: List<Pair<Int, Int>> by derivedStateOf {
        itens.mapNotNull { it.listing.anoUltimaAvaliacao }
            .groupingBy { it }.eachCount()
            .toList().sortedBy { it.first }
    }

    val listaOrdenada: List<ListingTratado> by derivedStateOf {
        when (ordenacao) {
            Ordenacao.PRECO_DESC -> itens.sortedByDescending { comum(it) }
            Ordenacao.PRECO_ASC -> itens.sortedBy { comum(it) }
            Ordenacao.AVALIACOES -> itens.sortedByDescending { it.listing.avaliacoes }
            Ordenacao.DISPONIBILIDADE -> itens.sortedByDescending { it.listing.disponibilidade365 }
            Ordenacao.BAIRRO -> itens.sortedBy { it.listing.bairro }
        }
    }

    // ------------------------------------------------------------------ ações

    fun limparFiltros() {
        filtro = FiltroState()
    }

    fun alternarCidade(cidade: String) {
        filtro = filtro.copy(cidades = filtro.cidades.alternar(cidade))
    }

    fun alternarTipo(tipo: String) {
        filtro = filtro.copy(tipos = filtro.tipos.alternar(tipo))
    }

    fun alternarBairro(bairro: String) {
        filtro = filtro.copy(bairros = filtro.bairros.alternar(bairro))
    }

    fun definirTaxa(moeda: String, taxa: Double) {
        ListingRepository.definirTaxa(moeda, taxa)
    }

    fun acrescentar(novos: List<Listing>) {
        ListingRepository.acrescentar(novos)
    }

    private fun Set<String>.alternar(v: String): Set<String> =
        if (contains(v)) this - v else this + v
}
