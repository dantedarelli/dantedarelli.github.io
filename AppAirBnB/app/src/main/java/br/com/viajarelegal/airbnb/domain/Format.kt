package br.com.viajarelegal.airbnb.domain

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/** Formatação numérica em pt-BR, sem depender de NumberFormat em cada chamada. */
object Format {

    private val PT = Locale("pt", "BR")

    private val SIMBOLOS = mapOf(
        "BRL" to "R$",
        "USD" to "US$",
        "EUR" to "€",
        "GBP" to "£",
        "ARS" to "AR$",
        "CLP" to "CL$",
    )

    fun simbolo(moeda: String): String = SIMBOLOS[moeda.uppercase()] ?: moeda.uppercase()

    fun numero(valor: Double, casas: Int = 1): String =
        String.format(PT, "%,.${casas}f", valor)

    fun inteiro(valor: Number): String = String.format(PT, "%,d", valor.toLong())

    fun moeda(valor: Double, moeda: String, casas: Int = 0): String =
        "${simbolo(moeda)} ${numero(valor, casas)}"

    /** Versão curta para caber em cartões: 1,2 mil · 3,4 mi. */
    fun compacto(valor: Double): String {
        val a = abs(valor)
        return when {
            a >= 1_000_000 -> numero(valor / 1_000_000, 1) + " mi"
            a >= 1_000 -> numero(valor / 1_000, 1) + " mil"
            a >= 10 -> valor.roundToLong().toString()
            else -> numero(valor, 1)
        }
    }

    fun moedaCompacta(valor: Double, moeda: String): String = "${simbolo(moeda)} ${compacto(valor)}"

    fun percentual(fracao: Double, casas: Int = 1): String = numero(fracao * 100, casas) + "%"

    /** "2025-05-31" → "31/05/2025"; devolve o original se não reconhecer. */
    fun data(iso: String): String {
        val p = iso.trim().split("-")
        if (p.size != 3 || p[0].length != 4) return iso
        return "${p[2].padStart(2, '0')}/${p[1].padStart(2, '0')}/${p[0]}"
    }

    fun plural(n: Int, singular: String, plural: String): String =
        "${inteiro(n)} ${if (n == 1) singular else plural}"
}
