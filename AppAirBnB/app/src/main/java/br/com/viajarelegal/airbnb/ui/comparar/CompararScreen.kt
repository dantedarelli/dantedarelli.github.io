package br.com.viajarelegal.airbnb.ui.comparar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import br.com.viajarelegal.airbnb.data.CsvParser
import br.com.viajarelegal.airbnb.domain.Format
import br.com.viajarelegal.airbnb.ui.DashboardViewModel
import br.com.viajarelegal.airbnb.ui.components.CartaoIndicador
import br.com.viajarelegal.airbnb.ui.components.EstadoVazio
import br.com.viajarelegal.airbnb.ui.components.LinhaMetrica
import br.com.viajarelegal.airbnb.ui.components.Painel
import br.com.viajarelegal.airbnb.ui.components.TituloSecao
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette
import br.com.viajarelegal.airbnb.ui.theme.categoryColor

/**
 * Comparação entre cidades. Como as bases estão em moedas diferentes, a tela
 * separa três leituras: valores na moeda local, valores convertidos por uma taxa
 * que o usuário controla, e indicadores adimensionais — os únicos que não
 * dependem de câmbio nenhum.
 */
@Composable
fun CompararScreen(vm: DashboardViewModel) {
    val p = LocalAppPalette.current
    val cidades = vm.porCidade

    if (cidades.isEmpty()) {
        EstadoVazio(
            "Nada para comparar",
            "Importe outra cidade ou afrouxe os filtros.",
            Modifier.fillMaxSize(),
        )
        return
    }

    // Texto em edição por moeda, para não brigar com o usuário enquanto digita.
    val rascunho = remember { mutableStateMapOf<String, String>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        // ------------------------------------------------------ visão por cidade
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                cidades.forEachIndexed { i, c ->
                    CartaoIndicador(
                        rotulo = c.cidade,
                        valor = Format.moeda(c.resumoLocal.mediana, c.moeda),
                        apoio = "${Format.inteiro(c.resumoLocal.n)} anúncios · mediana local",
                        destaque = categoryColor(i),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // ------------------------------------------------------------- câmbio
        item {
            Painel(
                titulo = "Moeda e conversão",
                apoio = "a taxa é sua: altere para testar cenários",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    cidades.map { it.moeda }.distinct().forEach { moeda ->
                        val atual = vm.taxas[moeda] ?: 1.0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "1 ${Format.simbolo(moeda)} equivale a (R$)",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    "$moeda → BRL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = p.faint,
                                )
                            }
                            OutlinedTextField(
                                value = rascunho[moeda] ?: Format.numero(atual, 2),
                                onValueChange = { texto ->
                                    rascunho[moeda] = texto
                                    CsvParser.numero(texto)?.takeIf { it > 0 }?.let {
                                        vm.definirTaxa(moeda, it)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(11.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = p.line,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(132.dp),
                            )
                        }
                    }
                    Text(
                        "A conversão afeta apenas as comparações em base comum. " +
                            "Os valores na moeda local permanecem intocados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = p.faint,
                    )
                }
            }
        }

        // --------------------------------------------------- métricas lado a lado
        item {
            Painel(titulo = "Métricas lado a lado") {
                Column {
                    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Box(Modifier.weight(1.25f))
                        cidades.forEachIndexed { i, c ->
                            Text(
                                c.cidade,
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = categoryColor(i),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    HorizontalDivider(color = p.line)

                    TituloSecao("Na moeda local", Modifier.padding(top = 10.dp, bottom = 2.dp))
                    LinhaMetrica("Anúncios", cidades.map { Format.inteiro(it.resumoLocal.n) })
                    LinhaMetrica("Moeda", cidades.map { it.moeda })
                    LinhaMetrica("Preço médio", cidades.map { Format.moeda(it.resumoLocal.media, it.moeda) })
                    LinhaMetrica("Mediana", cidades.map { Format.moeda(it.resumoLocal.mediana, it.moeda) })
                    LinhaMetrica("P25", cidades.map { Format.moeda(it.resumoLocal.p25, it.moeda) })
                    LinhaMetrica("P75", cidades.map { Format.moeda(it.resumoLocal.p75, it.moeda) })
                    LinhaMetrica("Máximo", cidades.map { Format.moedaCompacta(it.resumoLocal.maximo, it.moeda) })

                    HorizontalDivider(color = p.line, modifier = Modifier.padding(top = 8.dp))
                    TituloSecao("Em base comum (R$)", Modifier.padding(top = 10.dp, bottom = 2.dp))
                    val maisCaro = cidades.indices.maxByOrNull { cidades[it].resumoComum.mediana }
                    LinhaMetrica(
                        "Mediana convertida",
                        cidades.map { Format.moeda(it.resumoComum.mediana, "BRL") },
                        destaqueIndice = maisCaro,
                    )
                    LinhaMetrica("Média convertida", cidades.map { Format.moeda(it.resumoComum.media, "BRL") })
                    LinhaMetrica("Faixa interquartil", cidades.map { Format.moedaCompacta(it.resumoComum.iqr, "BRL") })

                    HorizontalDivider(color = p.line, modifier = Modifier.padding(top = 8.dp))
                    TituloSecao("Independentes de câmbio", Modifier.padding(top = 10.dp, bottom = 2.dp))
                    val maisDisperso = cidades.indices.maxByOrNull { cidades[it].resumoLocal.coefVariacao }
                    LinhaMetrica(
                        "Coeficiente de variação",
                        cidades.map { Format.percentual(it.resumoLocal.coefVariacao) },
                        destaqueIndice = maisDisperso,
                    )
                    LinhaMetrica(
                        "Assimetria (média/mediana)",
                        cidades.map { Format.numero(if (it.resumoLocal.mediana > 0) it.resumoLocal.media / it.resumoLocal.mediana else 0.0, 2) },
                    )
                    LinhaMetrica("Bairros distintos", cidades.map { Format.inteiro(it.bairrosDistintos) })
                    LinhaMetrica("Noites mínimas (média)", cidades.map { Format.numero(it.noitesMinimasMedia, 1) })
                    LinhaMetrica(
                        "Disponibilidade média",
                        cidades.map { "${Format.numero(it.disponibilidadeMedia, 0)} d" },
                    )
                    val maisOcupada = cidades.indices.maxByOrNull { cidades[it].ocupacaoEstimada }
                    LinhaMetrica(
                        "Ocupação estimada",
                        cidades.map { Format.percentual(it.ocupacaoEstimada) },
                        destaqueIndice = maisOcupada,
                    )
                    LinhaMetrica("Avaliações (média)", cidades.map { Format.numero(it.avaliacoesMedia, 1) })
                    LinhaMetrica("Extremos detectados", cidades.map { Format.inteiro(it.extremosDetectados) })
                }
            }
        }

        // ------------------------------------------------------------- leitura
        item {
            Painel(titulo = "Leitura rápida") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val ordenadas = cidades.sortedByDescending { it.resumoComum.mediana }
                    val cara = ordenadas.first()
                    val barata = ordenadas.last()
                    if (cidades.size > 1) {
                        val razao = if (barata.resumoComum.mediana > 0)
                            cara.resumoComum.mediana / barata.resumoComum.mediana else 0.0
                        Text(
                            "Na taxa vigente, a diária mediana de ${cara.cidade} custa " +
                                "${Format.numero(razao, 2)}× a de ${barata.cidade}.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    val dispersa = cidades.maxBy { it.resumoLocal.coefVariacao }
                    Text(
                        "${dispersa.cidade} tem o mercado mais heterogêneo: coeficiente de " +
                            "variação de ${Format.percentual(dispersa.resumoLocal.coefVariacao)} — " +
                            "leitura que independe da moeda.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    val ocupada = cidades.maxBy { it.ocupacaoEstimada }
                    Text(
                        "Maior pressão de demanda em ${ocupada.cidade}, com ocupação estimada de " +
                            "${Format.percentual(ocupada.ocupacaoEstimada)} do calendário.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Box(Modifier.height(2.dp))
                    Text(
                        "A ocupação é estimada a partir de availability_365 e não é um dado " +
                            "declarado — trate como indicador relativo, não absoluto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = p.faint,
                    )
                }
            }
        }
    }
}
