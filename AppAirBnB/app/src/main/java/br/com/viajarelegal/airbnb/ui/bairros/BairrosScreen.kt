package br.com.viajarelegal.airbnb.ui.bairros

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.viajarelegal.airbnb.domain.Format
import br.com.viajarelegal.airbnb.ui.DashboardViewModel
import br.com.viajarelegal.airbnb.ui.MetricaBairro
import br.com.viajarelegal.airbnb.ui.components.Celula
import br.com.viajarelegal.airbnb.ui.components.EstadoVazio
import br.com.viajarelegal.airbnb.ui.components.Etiqueta
import br.com.viajarelegal.airbnb.ui.components.LegendaEscala
import br.com.viajarelegal.airbnb.ui.components.MapaCalor
import br.com.viajarelegal.airbnb.ui.components.Painel
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette
import br.com.viajarelegal.airbnb.ui.theme.categoryColor

/**
 * Aba de bairros: mapa de calor por área (nº de anúncios) e cor (métrica),
 * seguido do ranking completo. É a leitura territorial adaptada ao celular.
 */
@Composable
fun BairrosScreen(vm: DashboardViewModel) {
    val p = LocalAppPalette.current
    val fatias = vm.bairrosPorMetrica
    var selecionado by remember { mutableStateOf<String?>(null) }

    if (fatias.isEmpty()) {
        EstadoVazio(
            "Nenhum bairro no recorte atual",
            "Ajuste os filtros para voltar a ver a distribuição territorial.",
            Modifier.fillMaxSize(),
        )
        return
    }

    val formatar: (Double) -> String = { v ->
        when (vm.metricaBairro) {
            MetricaBairro.MEDIANA, MetricaBairro.MEDIA -> Format.moedaCompacta(v, "BRL")
            MetricaBairro.CONTAGEM -> Format.inteiro(v)
            MetricaBairro.DISPONIBILIDADE -> "${Format.numero(v, 0)} dias"
            MetricaBairro.AVALIACOES -> Format.numero(v, 0)
        }
    }

    val menor = fatias.minOf { it.valor }
    val maior = fatias.maxOf { it.valor }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        item {
            Painel(
                titulo = "Mapa de calor dos bairros",
                apoio = "área = nº de anúncios · cor = ${vm.metricaBairro.rotulo.lowercase()}",
            ) {
                Column {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        MetricaBairro.entries.forEach { m ->
                            Etiqueta(
                                texto = m.rotulo,
                                selecionada = vm.metricaBairro == m,
                                onClick = { vm.metricaBairro = m },
                            )
                        }
                    }

                    Box(Modifier.height(13.dp))

                    MapaCalor(
                        celulas = fatias.map { f ->
                            Celula(
                                rotulo = f.bairro,
                                apoio = formatar(f.valor),
                                peso = f.n.toDouble(),
                                intensidade = f.valor,
                            )
                        },
                        onSelecionar = { selecionado = it.rotulo },
                    )

                    Box(Modifier.height(11.dp))
                    LegendaEscala(formatar(menor), formatar(maior))

                    if (selecionado != null) {
                        val f = fatias.firstOrNull { it.bairro == selecionado }
                        if (f != null) {
                            Box(Modifier.height(11.dp))
                            Text(
                                "${f.bairro} · ${f.cidade} — ${formatar(f.valor)} em " +
                                    Format.plural(f.n, "anúncio", "anúncios"),
                                style = MaterialTheme.typography.bodySmall,
                                color = p.muted,
                            )
                        }
                    }
                }
            }
        }

        item {
            Painel(
                titulo = "Ranking completo",
                apoio = "${fatias.size} bairros com anúncios no recorte",
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Text("BAIRRO", Modifier.weight(1.6f), style = MaterialTheme.typography.labelSmall, color = p.faint)
                    Text("N", Modifier.weight(0.5f), style = MaterialTheme.typography.labelSmall, color = p.faint)
                    Text(
                        vm.metricaBairro.rotulo.uppercase(),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = p.faint,
                    )
                }
            }
        }

        items(fatias.sortedByDescending { it.valor }, key = { it.bairro + it.cidade }) { f ->
            val cidades = vm.facetas.cidades
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1.6f)) {
                    Text(
                        f.bairro,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        f.cidade,
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor(cidades.indexOf(f.cidade).coerceAtLeast(0)),
                    )
                }
                Text(
                    Format.inteiro(f.n),
                    Modifier.weight(0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    color = p.muted,
                )
                Text(
                    formatar(f.valor),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
