package br.com.viajarelegal.airbnb.ui.painel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.viajarelegal.airbnb.domain.Format
import br.com.viajarelegal.airbnb.ui.DashboardViewModel
import br.com.viajarelegal.airbnb.ui.components.Barra
import br.com.viajarelegal.airbnb.ui.components.CartaoIndicador
import br.com.viajarelegal.airbnb.ui.components.CaixaDados
import br.com.viajarelegal.airbnb.ui.components.Distintivo
import br.com.viajarelegal.airbnb.ui.components.EstadoVazio
import br.com.viajarelegal.airbnb.ui.components.Fatia
import br.com.viajarelegal.airbnb.ui.components.GraficoBarras
import br.com.viajarelegal.airbnb.ui.components.GraficoCaixa
import br.com.viajarelegal.airbnb.ui.components.GraficoHistograma
import br.com.viajarelegal.airbnb.ui.components.GraficoLinha
import br.com.viajarelegal.airbnb.ui.components.GraficoRosca
import br.com.viajarelegal.airbnb.ui.components.Painel
import br.com.viajarelegal.airbnb.ui.theme.Brand
import br.com.viajarelegal.airbnb.ui.theme.Brand2
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette
import br.com.viajarelegal.airbnb.ui.theme.categoryColor

/** Aba principal: indicadores e a leitura gráfica da base sob os filtros. */
@Composable
fun PainelScreen(vm: DashboardViewModel) {
    val p = LocalAppPalette.current
    val itens = vm.itens
    val resumo = vm.resumoGeral

    if (itens.isEmpty()) {
        EstadoVazio(
            "Nenhum anúncio no recorte atual",
            "Afrouxe os filtros ou o tratamento de extremos para voltar a ver dados.",
            Modifier.fillMaxSize(),
        )
        return
    }

    val brl = { v: Double -> Format.moedaCompacta(v, "BRL") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 14.dp, end = 14.dp, top = 8.dp, bottom = 92.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        // ------------------------------------------------------- indicadores
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CartaoIndicador(
                    rotulo = "Anúncios analisados",
                    valor = Format.inteiro(itens.size),
                    apoio = "de ${Format.inteiro(vm.totalBase)} na base",
                    icone = Icons.Filled.Apartment,
                    modifier = Modifier.weight(1f),
                )
                CartaoIndicador(
                    rotulo = "Preço mediano",
                    valor = brl(resumo.mediana),
                    apoio = "base comum · R$",
                    destaque = Brand,
                    icone = Icons.Filled.Payments,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CartaoIndicador(
                    rotulo = "Faixa central",
                    valor = "${brl(resumo.p25)} – ${brl(resumo.p75)}",
                    apoio = "intervalo interquartil",
                    icone = Icons.Filled.Straighten,
                    modifier = Modifier.weight(1f),
                )
                CartaoIndicador(
                    rotulo = "Extremos",
                    valor = Format.inteiro(vm.tratamento.detectados),
                    apoio = vm.config.metodo.rotulo + " · " + vm.config.acao.rotulo.lowercase(),
                    destaque = if (vm.tratamento.detectados > 0) p.warn else null,
                    icone = Icons.Filled.WarningAmber,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CartaoIndicador(
                    rotulo = "Disponibilidade média",
                    valor = "${Format.numero(itens.map { it.listing.disponibilidade365.toDouble() }.average(), 0)} dias",
                    apoio = "ocupação estimada ${Format.percentual(itens.map { it.listing.ocupacaoEstimada }.average())}",
                    icone = Icons.Filled.EventAvailable,
                    modifier = Modifier.weight(1f),
                )
                CartaoIndicador(
                    rotulo = "Avaliações",
                    valor = Format.numero(itens.map { it.listing.avaliacoes.toDouble() }.average(), 0),
                    apoio = "média por anúncio",
                    destaque = Brand2,
                    icone = Icons.Filled.Star,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // -------------------------------------------------------- filtros ativos
        if (!vm.filtro.vazio) {
            item {
                Painel(titulo = "Recorte em vigor") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        vm.filtro.ativos.forEach {
                            Text(
                                "· $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = p.muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------- histograma
        item {
            Painel(
                titulo = "Distribuição de preços",
                apoio = "classes de Freedman–Diaconis · valores em R$",
            ) {
                val bins = vm.histograma
                GraficoHistograma(
                    contagens = bins.map { it.contagem },
                    rotuloInicio = brl(bins.firstOrNull()?.inicio ?: 0.0),
                    rotuloMeio = "mediana ${brl(resumo.mediana)}",
                    rotuloFim = brl(bins.lastOrNull()?.fim ?: 0.0),
                    cor = Brand,
                )
            }
        }

        // ------------------------------------------------------ caixa por cidade
        item {
            Painel(
                titulo = "Dispersão por cidade",
                apoio = "caixa = 25% a 75% · hastes = mínimo e máximo",
            ) {
                GraficoCaixa(
                    caixas = vm.porCidade.mapIndexed { i, c ->
                        CaixaDados(
                            rotulo = "${c.cidade} · ${c.moeda}",
                            minimo = c.resumoComum.minimo,
                            p25 = c.resumoComum.p25,
                            mediana = c.resumoComum.mediana,
                            p75 = c.resumoComum.p75,
                            maximo = c.resumoComum.maximo,
                            cor = categoryColor(i),
                        )
                    },
                    formatarValor = brl,
                )
            }
        }

        // ------------------------------------------------------------- bairros
        item {
            Painel(
                titulo = "Bairros mais caros",
                apoio = "preço mediano · mínimo de 5 anúncios",
            ) {
                if (vm.topBairros.isEmpty()) {
                    EstadoVazio("Poucos anúncios por bairro", "Nenhum bairro atingiu o piso de 5 anúncios.")
                } else {
                    GraficoBarras(
                        barras = vm.topBairros.mapIndexed { i, b ->
                            Barra(
                                rotulo = b.bairro,
                                valor = b.valor,
                                apoio = "${b.cidade} · ${Format.plural(b.n, "anúncio", "anúncios")}",
                                cor = if (i < 3) Brand else Brand.copy(alpha = 0.62f),
                            )
                        },
                        formatarValor = brl,
                    )
                }
            }
        }

        // ------------------------------------------------------- tipo de imóvel
        item {
            Painel(titulo = "Composição por tipo de acomodação") {
                val total = vm.porTipo.sumOf { it.n }.coerceAtLeast(1)
                GraficoRosca(
                    fatias = vm.porTipo.mapIndexed { i, t ->
                        Fatia(
                            rotulo = t.tipo,
                            valor = t.n.toDouble(),
                            cor = categoryColor(i),
                            apoio = "${Format.inteiro(t.n)} · ${Format.percentual(t.n.toDouble() / total, 0)}",
                        )
                    },
                )
            }
        }

        item {
            Painel(
                titulo = "Preço mediano por tipo",
                apoio = "base comum · R$",
            ) {
                GraficoBarras(
                    barras = vm.porTipo.mapIndexed { i, t ->
                        Barra(
                            rotulo = t.tipo,
                            valor = t.medianaComum,
                            apoio = Format.plural(t.n, "anúncio", "anúncios"),
                            cor = categoryColor(i),
                        )
                    },
                    formatarValor = brl,
                )
            }
        }

        // ------------------------------------------------------------ atividade
        item {
            Painel(
                titulo = "Atividade por ano da última avaliação",
                apoio = "anúncios sem avaliação ficam de fora",
            ) {
                GraficoLinha(pontos = vm.linhaDoTempo, cor = Brand2)
            }
        }

        // -------------------------------------------------------------- rodapé
        item {
            Painel {
                Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Distintivo("BASE EMBUTIDA", Brand2)
                        Box(Modifier.padding(3.dp))
                        if (vm.totalImportados > 0) {
                            Distintivo("+${vm.totalImportados} IMPORTADOS", Brand)
                        }
                    }
                    Box(Modifier.height(8.dp))
                    Text(
                        "Amostra de 600 anúncios (300 por cidade) extraída dos arquivos " +
                            "ny.csv e rj.csv no padrão Inside Airbnb. Nenhum dado sai do aparelho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = p.faint,
                    )
                    Box(Modifier.height(6.dp))
                    Text(
                        "Dante Darelli · dante.darelli@hotmail.com",
                        style = MaterialTheme.typography.labelSmall,
                        color = p.faint,
                    )
                }
            }
        }
    }
}
