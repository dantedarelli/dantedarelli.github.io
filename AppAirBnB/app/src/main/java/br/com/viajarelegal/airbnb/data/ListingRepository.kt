package br.com.viajarelegal.airbnb.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Base viva do aplicativo, inteiramente em memória — não há banco de dados.
 *
 * A carga inicial vem de [SeedData]; a tela de importação acrescenta linhas de
 * planilha ou digitadas à mão. Nada é gravado em disco: fechar o app volta ao
 * estado embutido, o que é proposital neste exemplo.
 */
object ListingRepository {

    var listings: List<Listing> by mutableStateOf(emptyList())
        private set

    /** Taxa de câmbio por moeda para a base comum (BRL). Editável pelo usuário. */
    val taxas = mutableStateMapOf<String, Double>()

    val moedasPorCidade: Map<String, String>
        get() = listings.associate { it.cidade to it.moeda }

    init { recarregarEmbutidos() }

    fun recarregarEmbutidos() {
        listings = parseSeed()
        taxas.clear()
        taxas.putAll(SeedData.TAXAS_PADRAO)
    }

    fun acrescentar(novos: List<Listing>) {
        if (novos.isEmpty()) return
        listings = listings + novos
        // Uma moeda nova entra com taxa 1,0 até o usuário informar a cotação.
        novos.map { it.moeda }.distinct().forEach { if (!taxas.containsKey(it)) taxas[it] = 1.0 }
    }

    fun acrescentar(novo: Listing) = acrescentar(listOf(novo))

    fun removerImportados() {
        listings = listings.filter { it.origem == Origem.EMBUTIDO }
    }

    fun definirTaxa(moeda: String, taxa: Double) {
        taxas[moeda] = taxa
    }

    fun proximoIdManual(): String = "MAN-" + (listings.count { it.origem == Origem.MANUAL } + 1)

    private fun parseSeed(): List<Listing> =
        SeedData.ROWS.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { linha ->
                val c = linha.split('|')
                if (c.size < 17) return@mapNotNull null
                Listing(
                    cidade = c[0],
                    moeda = c[1],
                    id = c[2],
                    nome = c[3],
                    anfitriao = c[4],
                    regiao = c[5],
                    bairro = c[6],
                    tipo = c[7],
                    latitude = c[8].toDoubleOrNull() ?: 0.0,
                    longitude = c[9].toDoubleOrNull() ?: 0.0,
                    preco = c[10].toDoubleOrNull() ?: 0.0,
                    noitesMinimas = c[11].toIntOrNull() ?: 1,
                    avaliacoes = c[12].toIntOrNull() ?: 0,
                    avaliacoesPorMes = c[13].toDoubleOrNull() ?: 0.0,
                    ultimaAvaliacao = c[14],
                    disponibilidade365 = c[15].toIntOrNull() ?: 0,
                    anunciosDoAnfitriao = c[16].toIntOrNull() ?: 1,
                    origem = Origem.EMBUTIDO,
                )
            }
            .toList()
}
