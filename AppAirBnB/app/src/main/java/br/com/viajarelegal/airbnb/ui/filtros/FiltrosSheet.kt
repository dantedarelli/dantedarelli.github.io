package br.com.viajarelegal.airbnb.ui.filtros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.viajarelegal.airbnb.domain.AcaoOutlier
import br.com.viajarelegal.airbnb.domain.Format
import br.com.viajarelegal.airbnb.domain.MetodoOutlier
import br.com.viajarelegal.airbnb.ui.DashboardViewModel
import br.com.viajarelegal.airbnb.ui.components.Etiqueta
import br.com.viajarelegal.airbnb.ui.components.TituloSecao
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette
import br.com.viajarelegal.airbnb.ui.theme.categoryColor

/**
 * Folha de filtros. Reúne, em uma tela de celular, o que no dashboard web ocupa
 * a barra lateral inteira: recorte da base e tratamento de valores extremos.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltrosSheet(vm: DashboardViewModel, onFechar: () -> Unit) {
    val p = LocalAppPalette.current
    val facetas = vm.facetas
    val estado = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var buscaBairro by remember { mutableStateOf("") }

    val precoMin = facetas.precoMinComum.toFloat()
    val precoMax = facetas.precoMaxComum.toFloat().coerceAtLeast(precoMin + 1f)
    // Coagido ao intervalo da base: um filtro herdado de outra importação fora
    // dos limites faria o RangeSlider lançar exceção.
    val inicioFaixa = (vm.filtro.precoMinComum?.toFloat() ?: precoMin).coerceIn(precoMin, precoMax)
    val fimFaixa = (vm.filtro.precoMaxComum?.toFloat() ?: precoMax).coerceIn(inicioFaixa, precoMax)
    val faixaAtual = inicioFaixa..fimFaixa

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = estado,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 30.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Filtros e tratamento", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${Format.inteiro(vm.itens.size)} de ${Format.inteiro(vm.totalBase)} anúncios no recorte",
                        style = MaterialTheme.typography.bodySmall,
                        color = p.muted,
                    )
                }
                OutlinedButton(onClick = { vm.limparFiltros() }) { Text("Limpar") }
            }

            Box(Modifier.padding(top = 16.dp))
            TituloSecao("Cidades")
            Box(Modifier.padding(top = 8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                facetas.cidades.forEachIndexed { i, c ->
                    Etiqueta(
                        texto = c,
                        selecionada = vm.filtro.cidades.contains(c),
                        cor = categoryColor(i),
                        onClick = { vm.alternarCidade(c) },
                    )
                }
            }

            Box(Modifier.padding(top = 16.dp))
            TituloSecao("Tipo de acomodação")
            Box(Modifier.padding(top = 8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                facetas.tipos.forEach { t ->
                    Etiqueta(
                        texto = t,
                        selecionada = vm.filtro.tipos.contains(t),
                        onClick = { vm.alternarTipo(t) },
                    )
                }
            }

            // -------------------------------------------------------- preço
            Box(Modifier.padding(top = 18.dp))
            TituloSecao("Faixa de preço (base comum R$)")
            Text(
                "${Format.moedaCompacta(faixaAtual.start.toDouble(), "BRL")} — " +
                    Format.moedaCompacta(faixaAtual.endInclusive.toDouble(), "BRL"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp),
            )
            RangeSlider(
                value = faixaAtual,
                onValueChange = { nova ->
                    vm.filtro = vm.filtro.copy(
                        precoMinComum = nova.start.toDouble().takeIf { it > precoMin },
                        precoMaxComum = nova.endInclusive.toDouble().takeIf { it < precoMax },
                    )
                },
                valueRange = precoMin..precoMax,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = p.line,
                ),
            )

            // ------------------------------------------------- demanda e estadia
            Box(Modifier.padding(top = 6.dp))
            TituloSecao("Demanda e estadia")

            ControleDeslizante(
                rotulo = "Máximo de noites mínimas",
                valor = (vm.filtro.noitesMaximas ?: 60).toFloat(),
                intervalo = 1f..60f,
                texto = if (vm.filtro.noitesMaximas == null) "todos" else "${vm.filtro.noitesMaximas}",
                onValueChange = {
                    vm.filtro = vm.filtro.copy(noitesMaximas = it.toInt().takeIf { v -> v < 60 })
                },
            )
            ControleDeslizante(
                rotulo = "Disponibilidade mínima (dias/ano)",
                valor = vm.filtro.disponibilidadeMinima.toFloat(),
                intervalo = 0f..365f,
                texto = "${vm.filtro.disponibilidadeMinima}",
                onValueChange = {
                    vm.filtro = vm.filtro.copy(disponibilidadeMinima = it.toInt())
                },
            )
            ControleDeslizante(
                rotulo = "Mínimo de avaliações",
                valor = vm.filtro.avaliacoesMinimas.toFloat(),
                intervalo = 0f..200f,
                texto = "${vm.filtro.avaliacoesMinimas}",
                onValueChange = {
                    vm.filtro = vm.filtro.copy(avaliacoesMinimas = it.toInt())
                },
            )

            // ------------------------------------------------------- outliers
            Box(Modifier.padding(top = 10.dp))
            HorizontalDivider(color = p.line)
            Box(Modifier.padding(top = 14.dp))
            TituloSecao("Tratamento de valores extremos")
            Text(
                "${Format.inteiro(vm.tratamento.detectados)} anúncios sinalizados — os limites " +
                    "são calculados por cidade, já que as moedas são diferentes.",
                style = MaterialTheme.typography.bodySmall,
                color = p.faint,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                MetodoOutlier.entries.forEach { m ->
                    Etiqueta(
                        texto = m.rotulo,
                        selecionada = vm.config.metodo == m,
                        onClick = { vm.config = vm.config.copy(metodo = m) },
                    )
                }
            }

            if (vm.config.metodo != MetodoOutlier.NENHUM) {
                Box(Modifier.padding(top = 10.dp))
                FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                    AcaoOutlier.entries.forEach { a ->
                        Etiqueta(
                            texto = a.rotulo,
                            selecionada = vm.config.acao == a,
                            onClick = { vm.config = vm.config.copy(acao = a) },
                        )
                    }
                }
            }

            when (vm.config.metodo) {
                MetodoOutlier.IQR -> ControleDeslizante(
                    rotulo = "Multiplicador k do IQR",
                    valor = vm.config.k.toFloat(),
                    intervalo = 0.5f..4f,
                    texto = Format.numero(vm.config.k, 1),
                    onValueChange = { vm.config = vm.config.copy(k = (it * 10).toInt() / 10.0) },
                )
                MetodoOutlier.MAD -> ControleDeslizante(
                    rotulo = "Limite |Z| modificado",
                    valor = vm.config.limiteZ.toFloat(),
                    intervalo = 1f..8f,
                    texto = Format.numero(vm.config.limiteZ, 1),
                    onValueChange = { vm.config = vm.config.copy(limiteZ = (it * 10).toInt() / 10.0) },
                )
                MetodoOutlier.PERCENTIL -> {
                    ControleDeslizante(
                        rotulo = "Percentil inferior",
                        valor = vm.config.percentilBaixo.toFloat(),
                        intervalo = 0f..20f,
                        texto = "${Format.numero(vm.config.percentilBaixo, 1)}%",
                        onValueChange = { vm.config = vm.config.copy(percentilBaixo = (it * 10).toInt() / 10.0) },
                    )
                    ControleDeslizante(
                        rotulo = "Percentil superior",
                        valor = vm.config.percentilAlto.toFloat(),
                        intervalo = 80f..100f,
                        texto = "${Format.numero(vm.config.percentilAlto, 1)}%",
                        onValueChange = { vm.config = vm.config.copy(percentilAlto = (it * 10).toInt() / 10.0) },
                    )
                }
                MetodoOutlier.NENHUM -> Unit
            }

            // --------------------------------------------------------- bairros
            Box(Modifier.padding(top = 14.dp))
            HorizontalDivider(color = p.line)
            Box(Modifier.padding(top = 14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TituloSecao("Bairros", Modifier.weight(1f))
                Text(
                    if (vm.filtro.bairros.isEmpty()) "todos" else "${vm.filtro.bairros.size} marcados",
                    style = MaterialTheme.typography.labelSmall,
                    color = p.faint,
                )
            }
            Box(Modifier.padding(top = 8.dp))
            androidx.compose.material3.OutlinedTextField(
                value = buscaBairro,
                onValueChange = { buscaBairro = it },
                placeholder = { Text("Filtrar lista de bairros") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(Modifier.padding(top = 10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                facetas.bairros
                    .filter { buscaBairro.isBlank() || it.contains(buscaBairro, ignoreCase = true) }
                    .take(60)
                    .forEach { b ->
                        Etiqueta(
                            texto = b,
                            selecionada = vm.filtro.bairros.contains(b),
                            onClick = { vm.alternarBairro(b) },
                        )
                    }
            }

            Box(Modifier.padding(top = 20.dp))
            Button(
                onClick = onFechar,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Ver ${Format.inteiro(vm.itens.size)} anúncios")
            }
        }
    }
}

@Composable
private fun ControleDeslizante(
    rotulo: String,
    valor: Float,
    intervalo: ClosedFloatingPointRange<Float>,
    texto: String,
    onValueChange: (Float) -> Unit,
) {
    val p = LocalAppPalette.current
    Column(Modifier.padding(top = 10.dp)) {
        Row {
            Text(
                rotulo,
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = p.muted,
            )
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = valor.coerceIn(intervalo.start, intervalo.endInclusive),
            onValueChange = onValueChange,
            valueRange = intervalo,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = p.line,
            ),
        )
    }
}
