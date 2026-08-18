package br.com.viajarelegal.airbnb.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette

/** Painel padrão do aplicativo: mesmo raio, borda e respiro do dashboard web. */
@Composable
fun Painel(
    modifier: Modifier = Modifier,
    titulo: String? = null,
    apoio: String? = null,
    acao: @Composable (() -> Unit)? = null,
    conteudo: @Composable () -> Unit,
) {
    val p = LocalAppPalette.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, p.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            if (titulo != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(titulo, style = MaterialTheme.typography.titleMedium)
                        if (apoio != null) {
                            Text(
                                apoio,
                                style = MaterialTheme.typography.bodySmall,
                                color = p.faint,
                            )
                        }
                    }
                    acao?.invoke()
                }
                Box(Modifier.height(12.dp))
            }
            conteudo()
        }
    }
}

/** Rótulo de seção em caixa alta, como os "h-section" do painel web. */
@Composable
fun TituloSecao(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        color = LocalAppPalette.current.muted,
    )
}

/** Cartão de indicador: valor grande, rótulo e nota de apoio. */
@Composable
fun CartaoIndicador(
    rotulo: String,
    valor: String,
    apoio: String? = null,
    destaque: Color? = null,
    icone: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val p = LocalAppPalette.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, p.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icone != null) {
                    Icon(
                        icone,
                        contentDescription = null,
                        tint = destaque ?: p.muted,
                        modifier = Modifier.size(15.dp).padding(end = 0.dp),
                    )
                    Box(Modifier.size(6.dp))
                }
                Text(
                    rotulo.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = p.faint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(Modifier.height(6.dp))
            Text(
                valor,
                style = MaterialTheme.typography.headlineSmall,
                color = destaque ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (apoio != null) {
                Box(Modifier.height(3.dp))
                Text(
                    apoio,
                    style = MaterialTheme.typography.bodySmall,
                    color = p.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Etiqueta selecionável — usada para cidades, tipos e bairros. */
@Composable
fun Etiqueta(
    texto: String,
    selecionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color? = null,
    contagem: Int? = null,
) {
    val p = LocalAppPalette.current
    val fundo = when {
        selecionada && cor != null -> cor.copy(alpha = 0.20f)
        selecionada -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borda = when {
        selecionada && cor != null -> cor
        selecionada -> MaterialTheme.colorScheme.primary
        else -> p.line
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fundo)
            .border(1.dp, borda, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (cor != null) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(cor))
        }
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (contagem != null) {
            Text(
                contagem.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = p.faint,
            )
        }
    }
}

/** Distintivo compacto para status e origem de dados. */
@Composable
fun Distintivo(texto: String, cor: Color, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(cor.copy(alpha = 0.16f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = cor,
        fontWeight = FontWeight.SemiBold,
    )
}

/** Linha "rótulo → valor" usada nas tabelas de comparação. */
@Composable
fun LinhaMetrica(
    rotulo: String,
    valores: List<String>,
    destaqueIndice: Int? = null,
    modifier: Modifier = Modifier,
) {
    val p = LocalAppPalette.current
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            rotulo,
            modifier = Modifier.weight(1.25f),
            style = MaterialTheme.typography.bodySmall,
            color = p.muted,
        )
        valores.forEachIndexed { i, v ->
            Text(
                v,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (i == destaqueIndice) FontWeight.Bold else FontWeight.Medium,
                color = if (i == destaqueIndice) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Estado vazio: nenhum anúncio sobrou depois dos filtros. */
@Composable
fun EstadoVazio(titulo: String, descricao: String, modifier: Modifier = Modifier) {
    val p = LocalAppPalette.current
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("🔎", style = MaterialTheme.typography.headlineSmall)
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            descricao,
            style = MaterialTheme.typography.bodySmall,
            color = p.muted,
        )
    }
}
