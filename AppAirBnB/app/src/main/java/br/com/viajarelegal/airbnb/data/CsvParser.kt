package br.com.viajarelegal.airbnb.data

/**
 * Leitor de CSV tolerante, escrito à mão para não depender de biblioteca externa.
 *
 * Cobre as armadilhas encontradas nos arquivos reais do Inside Airbnb:
 *  - campos entre aspas contendo vírgulas, aspas escapadas ("") e quebras de linha;
 *  - separador variável (`,` `;` tabulação ou `|`), detectado pelo cabeçalho;
 *  - BOM UTF-8 no início do arquivo;
 *  - números com vírgula decimal e separador de milhar.
 */
object CsvParser {

    data class Tabela(val cabecalho: List<String>, val linhas: List<List<String>>) {
        val vazia: Boolean get() = linhas.isEmpty()
    }

    private val SEPARADORES = charArrayOf(',', ';', '\t', '|')

    /** Escolhe o separador que produz mais colunas na primeira linha lógica. */
    fun detectarSeparador(texto: String): Char {
        val primeira = primeiraLinhaLogica(texto)
        return SEPARADORES.maxByOrNull { sep -> dividirLinha(primeira, sep).size } ?: ','
    }

    fun parse(textoBruto: String, separador: Char? = null): Tabela {
        val texto = textoBruto.removePrefix("﻿")
        if (texto.isBlank()) return Tabela(emptyList(), emptyList())

        val sep = separador ?: detectarSeparador(texto)
        val registros = dividirRegistros(texto, sep)
        if (registros.isEmpty()) return Tabela(emptyList(), emptyList())

        val cabecalho = registros.first().map { it.trim().removeSurrounding("\"").trim() }
        val linhas = registros.drop(1)
            .filter { linha -> linha.any { it.isNotBlank() } }
            .map { linha ->
                // Normaliza o comprimento para o do cabeçalho: sobras são descartadas,
                // faltas viram string vazia. Evita IndexOutOfBounds em arquivos torto.
                List(cabecalho.size) { i -> linha.getOrElse(i) { "" }.trim() }
            }
        return Tabela(cabecalho, linhas)
    }

    /** Divide o texto inteiro respeitando aspas (que podem conter `\n`). */
    private fun dividirRegistros(texto: String, sep: Char): List<List<String>> {
        val registros = mutableListOf<List<String>>()
        var campos = mutableListOf<String>()
        val atual = StringBuilder()
        var dentroDeAspas = false
        var i = 0

        fun fecharCampo() {
            campos.add(atual.toString())
            atual.setLength(0)
        }

        fun fecharRegistro() {
            fecharCampo()
            registros.add(campos)
            campos = mutableListOf()
        }

        while (i < texto.length) {
            val c = texto[i]
            when {
                dentroDeAspas && c == '"' && i + 1 < texto.length && texto[i + 1] == '"' -> {
                    atual.append('"'); i++
                }
                c == '"' -> dentroDeAspas = !dentroDeAspas
                !dentroDeAspas && c == sep -> fecharCampo()
                !dentroDeAspas && c == '\r' -> Unit // ignora: o \n seguinte fecha o registro
                !dentroDeAspas && c == '\n' -> fecharRegistro()
                else -> atual.append(c)
            }
            i++
        }
        if (atual.isNotEmpty() || campos.isNotEmpty()) fecharRegistro()
        return registros
    }

    private fun primeiraLinhaLogica(texto: String): String {
        val fim = texto.indexOfFirst { it == '\n' || it == '\r' }
        return if (fim < 0) texto else texto.substring(0, fim)
    }

    private fun dividirLinha(linha: String, sep: Char): List<String> {
        val partes = mutableListOf<String>()
        val atual = StringBuilder()
        var aspas = false
        for (c in linha) {
            when {
                c == '"' -> aspas = !aspas
                c == sep && !aspas -> { partes.add(atual.toString()); atual.setLength(0) }
                else -> atual.append(c)
            }
        }
        partes.add(atual.toString())
        return partes
    }

    /**
     * Converte texto em número aceitando os dois padrões decimais.
     *
     * "1.234,56" → 1234.56 · "1,234.56" → 1234.56 · "0,211" → 0.211 · "" → null
     *
     * Um separador repetido é milhar; um separador sozinho é decimal — regra que
     * preserva latitudes ("-73.953") e avaliações por mês ("0.21") sem adivinhação.
     */
    fun numero(bruto: String?): Double? {
        if (bruto.isNullOrBlank()) return null
        var s = bruto.trim().filter { it.isDigit() || it == ',' || it == '.' || it == '-' || it == '+' }
        if (s.isBlank()) return null

        val virgulas = s.count { it == ',' }
        val pontos = s.count { it == '.' }
        s = when {
            // Os dois presentes: o que aparece por último é o separador decimal.
            virgulas > 0 && pontos > 0 ->
                if (s.lastIndexOf(',') > s.lastIndexOf('.')) s.replace(".", "").replace(',', '.')
                else s.replace(",", "")
            // Repetido é sempre milhar ("1.234.567"); sozinho é sempre decimal ("0,211").
            virgulas > 1 -> s.replace(",", "")
            pontos > 1 -> s.replace(".", "")
            virgulas == 1 -> s.replace(',', '.')
            else -> s
        }
        return s.toDoubleOrNull()
    }

    fun inteiro(bruto: String?): Int? = numero(bruto)?.toInt()
}
