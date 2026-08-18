package br.com.viajarelegal.airbnb.domain

import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/** Resumo estatístico de uma amostra de preços. */
data class Resumo(
    val n: Int,
    val media: Double,
    val mediana: Double,
    val minimo: Double,
    val maximo: Double,
    val p25: Double,
    val p75: Double,
    val p05: Double,
    val p95: Double,
    val desvioPadrao: Double,
) {
    val iqr: Double get() = p75 - p25
    /** Coeficiente de variação — comparável entre moedas diferentes. */
    val coefVariacao: Double get() = if (media == 0.0) 0.0 else desvioPadrao / media

    companion object {
        val VAZIO = Resumo(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    }
}

object Stats {

    /** Percentil por interpolação linear (mesmo método do dashboard web). */
    fun percentil(ordenados: List<Double>, p: Double): Double {
        if (ordenados.isEmpty()) return 0.0
        if (ordenados.size == 1) return ordenados[0]
        val pos = (ordenados.size - 1) * p.coerceIn(0.0, 1.0)
        val baixo = floor(pos).toInt()
        val alto = ceil(pos).toInt()
        if (baixo == alto) return ordenados[baixo]
        val peso = pos - baixo
        return ordenados[baixo] * (1 - peso) + ordenados[alto] * peso
    }

    fun mediana(valores: List<Double>): Double = percentil(valores.sorted(), 0.5)

    fun resumir(valores: List<Double>): Resumo {
        if (valores.isEmpty()) return Resumo.VAZIO
        val ord = valores.sorted()
        val media = ord.sum() / ord.size
        val variancia = if (ord.size < 2) 0.0 else ord.sumOf { (it - media).pow(2) } / (ord.size - 1)
        return Resumo(
            n = ord.size,
            media = media,
            mediana = percentil(ord, 0.5),
            minimo = ord.first(),
            maximo = ord.last(),
            p25 = percentil(ord, 0.25),
            p75 = percentil(ord, 0.75),
            p05 = percentil(ord, 0.05),
            p95 = percentil(ord, 0.95),
            desvioPadrao = sqrt(variancia),
        )
    }

    /** Desvio absoluto mediano — base do Z-score modificado. */
    fun mad(valores: List<Double>): Double {
        if (valores.isEmpty()) return 0.0
        val med = mediana(valores)
        return mediana(valores.map { abs(it - med) })
    }

    /** Uma barra do histograma. */
    data class Bin(val inicio: Double, val fim: Double, val contagem: Int)

    /**
     * Largura de classe de Freedman–Diaconis (2·IQR/∛n), com recuo para a regra
     * de Sturges quando o IQR é zero (amostra muito concentrada).
     */
    fun histograma(valores: List<Double>, maxBins: Int = 24): List<Bin> {
        if (valores.isEmpty()) return emptyList()
        val ord = valores.sorted()
        val min = ord.first()
        val max = ord.last()
        if (max <= min) return listOf(Bin(min, max, ord.size))

        val iqr = percentil(ord, 0.75) - percentil(ord, 0.25)
        val larguraFd = if (iqr > 0) 2 * iqr / cbrt(ord.size.toDouble()) else 0.0
        val bins = if (larguraFd > 0) {
            ceil((max - min) / larguraFd).toInt()
        } else {
            ceil(ln(ord.size.toDouble()) / ln(2.0) + 1).toInt()
        }.coerceIn(4, maxBins)

        val largura = (max - min) / bins
        val contagens = IntArray(bins)
        for (v in ord) {
            val idx = (((v - min) / largura).toInt()).coerceIn(0, bins - 1)
            contagens[idx]++
        }
        return List(bins) { i -> Bin(min + i * largura, min + (i + 1) * largura, contagens[i]) }
    }
}
