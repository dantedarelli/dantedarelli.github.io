package br.com.viajarelegal.airbnb.domain

import br.com.viajarelegal.airbnb.data.Listing
import kotlin.math.abs

/** Métodos de detecção de valores extremos, iguais aos do dashboard web. */
enum class MetodoOutlier(val rotulo: String) {
    IQR("IQR (Tukey)"),
    PERCENTIL("Corte por percentil"),
    MAD("Z-score modificado"),
    NENHUM("Sem tratamento"),
}

/** O que fazer com o que foi detectado. */
enum class AcaoOutlier(val rotulo: String) {
    REMOVER("Remover"),
    WINSORIZAR("Winsorizar"),
    DESTACAR("Destacar"),
}

data class ConfigOutlier(
    val metodo: MetodoOutlier = MetodoOutlier.IQR,
    val acao: AcaoOutlier = AcaoOutlier.REMOVER,
    val k: Double = 1.5,
    val percentilBaixo: Double = 1.0,
    val percentilAlto: Double = 99.0,
    val limiteZ: Double = 3.5,
)

/** Anúncio já tratado: preço possivelmente ajustado e sinalização de extremo. */
data class ListingTratado(val listing: Listing, val preco: Double, val extremo: Boolean)

data class ResultadoOutliers(
    val itens: List<ListingTratado>,
    val detectados: Int,
    /**
     * Detecções por cidade. Precisa vir daqui e não de [itens]: com a ação
     * "Remover" os extremos somem da lista, mas a contagem continua sendo
     * informação relevante para o gestor.
     */
    val detectadosPorCidade: Map<String, Int>,
    val limiteInferior: Double?,
    val limiteSuperior: Double?,
)

object Outliers {

    /**
     * Aplica o tratamento sobre uma lista já filtrada. Os limites são calculados
     * por cidade, porque as moedas são diferentes e um corte único distorceria
     * a base de menor valor nominal.
     */
    fun aplicar(listings: List<Listing>, config: ConfigOutlier): ResultadoOutliers {
        if (listings.isEmpty() || config.metodo == MetodoOutlier.NENHUM) {
            return ResultadoOutliers(
                itens = listings.map { ListingTratado(it, it.preco, false) },
                detectados = 0,
                detectadosPorCidade = emptyMap(),
                limiteInferior = null,
                limiteSuperior = null,
            )
        }

        val porCidade = listings.groupBy { it.cidade }
        val resultado = mutableListOf<ListingTratado>()
        val porCidadeDetectados = mutableMapOf<String, Int>()
        var detectados = 0
        var infGlobal: Double? = null
        var supGlobal: Double? = null

        for ((cidade, doGrupo) in porCidade) {
            val precos = doGrupo.map { it.preco }
            val (inf, sup) = limites(precos, config)
            if (infGlobal == null || inf < infGlobal!!) infGlobal = inf
            if (supGlobal == null || sup > supGlobal!!) supGlobal = sup

            for (l in doGrupo) {
                val extremo = l.preco < inf || l.preco > sup
                if (extremo) {
                    detectados++
                    porCidadeDetectados[cidade] = (porCidadeDetectados[cidade] ?: 0) + 1
                }
                when {
                    !extremo -> resultado += ListingTratado(l, l.preco, false)
                    config.acao == AcaoOutlier.REMOVER -> Unit
                    config.acao == AcaoOutlier.WINSORIZAR ->
                        resultado += ListingTratado(l, l.preco.coerceIn(inf, sup), true)
                    else -> resultado += ListingTratado(l, l.preco, true)
                }
            }
        }
        return ResultadoOutliers(resultado, detectados, porCidadeDetectados, infGlobal, supGlobal)
    }

    private fun limites(precos: List<Double>, config: ConfigOutlier): Pair<Double, Double> {
        val ord = precos.sorted()
        return when (config.metodo) {
            MetodoOutlier.IQR -> {
                val q1 = Stats.percentil(ord, 0.25)
                val q3 = Stats.percentil(ord, 0.75)
                val iqr = q3 - q1
                Pair(q1 - config.k * iqr, q3 + config.k * iqr)
            }
            MetodoOutlier.PERCENTIL -> Pair(
                Stats.percentil(ord, config.percentilBaixo / 100.0),
                Stats.percentil(ord, config.percentilAlto / 100.0),
            )
            MetodoOutlier.MAD -> {
                val med = Stats.mediana(ord)
                val mad = Stats.mad(ord)
                // 0,6745 torna o MAD comparável ao desvio padrão de uma normal.
                if (mad == 0.0) {
                    Pair(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                } else {
                    val margem = config.limiteZ * mad / 0.6745
                    Pair(med - margem, med + margem)
                }
            }
            MetodoOutlier.NENHUM -> Pair(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
        }
    }

    /** Z-score modificado de um valor — usado para explicar por que algo é extremo. */
    fun zModificado(valor: Double, amostra: List<Double>): Double {
        val mad = Stats.mad(amostra)
        if (mad == 0.0) return 0.0
        return 0.6745 * (valor - Stats.mediana(amostra)) / mad
    }

    fun distanciaAbsoluta(valor: Double, referencia: Double): Double = abs(valor - referencia)
}
