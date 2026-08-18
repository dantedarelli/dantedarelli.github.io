package br.com.viajarelegal.airbnb.ui.anuncios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.viajarelegal.airbnb.data.Origem
import br.com.viajarelegal.airbnb.domain.Format
import br.com.viajarelegal.airbnb.domain.ListingTratado
import br.com.viajarelegal.airbnb.ui.DashboardViewModel
import br.com.viajarelegal.airbnb.ui.Ordenacao
import br.com.viajarelegal.airbnb.ui.components.Distintivo
import br.com.viajarelegal.airbnb.ui.components.EstadoVazio
import br.com.viajarelegal.airbnb.ui.components.Etiqueta
import br.com.viajarelegal.airbnb.ui.components.LinhaMetrica
import br.com.viajarelegal.airbnb.ui.components.TituloSecao
import br.com.viajarelegal.airbnb.ui.theme.Brand
import br.com.viajarelegal.airbnb.ui.theme.Brand2
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette
import br.com.viajarelegal.airbnb.ui.theme.categoryColor

/** Lista de anúncios com busca, ordenação e ficha completa em folha inferior. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnunciosScreen(vm: DashboardViewModel) {
    val p = LocalAppPalette.current
    val lista = vm.listaOrdenada
    var detalhe by remember { mutableStateOf<ListingTratado?>(null) }
    val estadoFolha = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(Modifier.fillMaxSize()) {

        OutlinedTextField(
            value = vm.filtro.busca,
            onValueChange = { vm.filtro = vm.filtro.copy(busca = it) },
            placeholder = { Text("Buscar por nome, bairro ou anfitrião") },
            singleLine = true,
            shape = RoundedCornerShape(13.dp),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = p.muted) },
            trailingIcon = {
                if (vm.filtro.busca.isNotEmpty()) {
                    IconButton(onClick = { vm.filtro = vm.filtro.copy(busca = "") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpar busca", tint = p.muted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = p.line,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )

        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Ordenacao.entries.forEach { o ->
                Etiqueta(
                    texto = o.rotulo,
                    selecionada = vm.ordenacao == o,
                    onClick = { vm.ordenacao = o },
                )
            }
        }

        Text(
            "${Format.inteiro(lista.size)} anúncios no recorte",
            style = MaterialTheme.typography.labelSmall,
            color = p.faint,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
        )

        if (lista.isEmpty()) {
            EstadoVazio(
                "Nenhum anúncio encontrado",
                "Tente outro termo de busca ou reveja os filtros.",
                Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                // A chave inclui a posição: importar duas vezes a mesma planilha
                // pode repetir ids, e chave duplicada derruba a LazyColumn.
                itemsIndexed(lista, key = { i, it -> "${it.listing.cidade}-${it.listing.id}-$i" }) { _, item ->
                    CartaoAnuncio(
                        item = item,
                        indiceCidade = vm.facetas.cidades.indexOf(item.listing.cidade).coerceAtLeast(0),
                        onClick = { detalhe = item },
                    )
                }
                item {
                    Box(Modifier.height(4.dp))
                }
            }
        }
    }

    val alvo = detalhe
    if (alvo != null) {
        ModalBottomSheet(
            onDismissRequest = { detalhe = null },
            sheetState = estadoFolha,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            FichaAnuncio(alvo, vm)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartaoAnuncio(
    item: ListingTratado,
    indiceCidade: Int,
    onClick: () -> Unit,
) {
    val p = LocalAppPalette.current
    val l = item.listing
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (item.extremo) p.warn.copy(alpha = 0.6f) else p.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(categoryColor(indiceCidade).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    l.cidade,
                    style = MaterialTheme.typography.labelMedium,
                    color = categoryColor(indiceCidade),
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(Modifier.size(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    l.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(Modifier.height(3.dp))
                Text(
                    "${l.bairro}${if (l.regiao.isNotBlank()) " · ${l.regiao}" else ""} · ${l.tipo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = p.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(Modifier.height(7.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        Format.moeda(item.preco, l.moeda),
                        style = MaterialTheme.typography.titleMedium,
                        color = Brand,
                    )
                    Text(
                        "/noite",
                        style = MaterialTheme.typography.labelSmall,
                        color = p.faint,
                    )
                    Box(Modifier.weight(1f))
                    Text(
                        "★ ${Format.inteiro(l.avaliacoes)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = p.muted,
                    )
                }
                if (item.extremo || l.origem != Origem.EMBUTIDO) {
                    Box(Modifier.height(7.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.extremo) Distintivo("EXTREMO", p.warn)
                        when (l.origem) {
                            Origem.PLANILHA -> Distintivo("IMPORTADO", Brand2)
                            Origem.MANUAL -> Distintivo("MANUAL", Brand2)
                            Origem.EMBUTIDO -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FichaAnuncio(item: ListingTratado, vm: DashboardViewModel) {
    val p = LocalAppPalette.current
    val l = item.listing
    val taxa = vm.taxas[l.moeda] ?: 1.0

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 34.dp),
    ) {
        Text(l.nome, style = MaterialTheme.typography.titleLarge)
        Box(Modifier.height(4.dp))
        Text(
            "${l.bairro} · ${l.cidade}",
            style = MaterialTheme.typography.bodyMedium,
            color = p.muted,
        )

        Box(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                Format.moeda(item.preco, l.moeda),
                style = MaterialTheme.typography.displaySmall,
                color = Brand,
            )
            Box(Modifier.size(8.dp))
            Text(
                "≈ ${Format.moeda(item.preco * taxa, "BRL")}",
                style = MaterialTheme.typography.bodySmall,
                color = p.faint,
            )
        }

        if (item.extremo) {
            Box(Modifier.height(10.dp))
            Text(
                "Sinalizado como valor extremo pelo método ${vm.config.metodo.rotulo}.",
                style = MaterialTheme.typography.bodySmall,
                color = p.warn,
            )
        }

        Box(Modifier.height(16.dp))
        TituloSecao("Ficha")
        LinhaMetrica("Tipo", listOf(l.tipo))
        LinhaMetrica("Região", listOf(l.regiao.ifBlank { "—" }))
        LinhaMetrica("Anfitrião", listOf(l.anfitriao))
        LinhaMetrica("Anúncios do anfitrião", listOf(Format.inteiro(l.anunciosDoAnfitriao)))
        LinhaMetrica("Noites mínimas", listOf(Format.inteiro(l.noitesMinimas)))
        LinhaMetrica("Avaliações", listOf(Format.inteiro(l.avaliacoes)))
        LinhaMetrica("Avaliações por mês", listOf(Format.numero(l.avaliacoesPorMes, 2)))
        LinhaMetrica(
            "Última avaliação",
            listOf(if (l.ultimaAvaliacao.isBlank()) "sem avaliações" else Format.data(l.ultimaAvaliacao)),
        )
        LinhaMetrica("Disponibilidade", listOf("${Format.inteiro(l.disponibilidade365)} dias/ano"))
        LinhaMetrica("Ocupação estimada", listOf(Format.percentual(l.ocupacaoEstimada)))
        LinhaMetrica(
            "Coordenadas",
            listOf("${Format.numero(l.latitude, 4)}, ${Format.numero(l.longitude, 4)}"),
        )
        LinhaMetrica(
            "Origem",
            listOf(
                when (l.origem) {
                    Origem.EMBUTIDO -> "Base embutida"
                    Origem.PLANILHA -> "Planilha importada"
                    Origem.MANUAL -> "Lançamento manual"
                },
            ),
        )
    }
}
