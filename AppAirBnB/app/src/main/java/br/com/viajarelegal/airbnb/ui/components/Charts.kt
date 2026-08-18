package br.com.viajarelegal.airbnb.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette
import br.com.viajarelegal.airbnb.ui.theme.SequentialScale
import kotlin.math.max
import kotlin.math.min

/**
 * Vai de 0 a 1 uma única vez, quando o gráfico entra em tela — é o que faz as
 * barras "crescerem" em vez de aparecerem prontas.
 */
@Composable
private fun progressoDeEntrada(duracaoMs: Int = 520, rotulo: String = "entrada"): Float {
    var iniciado by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { iniciado = true }
    val progresso by animateFloatAsState(
        targetValue = if (iniciado) 1f else 0f,
        animationSpec = tween(duracaoMs),
        label = rotulo,
    )
    return progresso
}

// ============================================================ histograma

/**
 * Histograma de preços. As barras são desenhadas em Canvas e os rótulos do eixo
 * ficam como texto composto, o que mantém a acessibilidade e o suporte a fontes
 * grandes do sistema.
 */
@Composable
fun GraficoHistograma(
    contagens: List<Int>,
    rotuloInicio: String,
    rotuloMeio: String,
    rotuloFim: String,
    cor: Color,
    modifier: Modifier = Modifier,
    altura: androidx.compose.ui.unit.Dp = 150.dp,
) {
    val p = LocalAppPalette.current
    val maximo = (contagens.maxOrNull() ?: 0).coerceAtLeast(1)
    val progresso = progressoDeEntrada(rotulo = "histograma")

    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(altura)) {
            if (contagens.isEmpty()) return@Canvas
            val vao = 2.dp.toPx()
            val larguraBarra = (size.width - vao * (contagens.size - 1)) / contagens.size
            // Linhas de grade em 1/4, 1/2 e 3/4 da altura.
            listOf(0.25f, 0.5f, 0.75f).forEach { f ->
                val y = size.height * f
                drawLine(p.line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            contagens.forEachIndexed { i, c ->
                val h = (c.toFloat() / maximo) * size.height * progresso
                if (h <= 0f) return@forEachIndexed
                drawRoundRect(
                    color = cor,
                    topLeft = Offset(i * (larguraBarra + vao), size.height - h),
                    size = Size(larguraBarra, h),
                    cornerRadius = CornerRadius(3f, 3f),
                )
            }
        }
        Box(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(rotuloInicio, rotuloMeio, rotuloFim).forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = p.faint)
            }
        }
    }
}

// ==================================================== barras horizontais

data class Barra(val rotulo: String, val valor: Double, val apoio: String, val cor: Color)

/** Ranking em barras horizontais — formato que lê bem em tela estreita. */
@Composable
fun GraficoBarras(
    barras: List<Barra>,
    modifier: Modifier = Modifier,
    formatarValor: (Double) -> String,
) {
    val p = LocalAppPalette.current
    val maximo = barras.maxOfOrNull { it.valor }?.takeIf { it > 0 } ?: 1.0

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        barras.forEach { b ->
            val fracao by animateFloatAsState(
                targetValue = (b.valor / maximo).toFloat().coerceIn(0.02f, 1f),
                animationSpec = tween(450),
                label = "barra-${b.rotulo}",
            )
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        b.rotulo,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        formatarValor(b.valor),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Box(Modifier.height(5.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(50))
                        .background(p.line.copy(alpha = 0.55f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fracao)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(b.cor),
                    )
                }
                Box(Modifier.height(3.dp))
                Text(b.apoio, style = MaterialTheme.typography.labelSmall, color = p.faint)
            }
        }
    }
}

// ============================================================== rosca

data class Fatia(val rotulo: String, val valor: Double, val cor: Color, val apoio: String)

/** Rosca com legenda ao lado — composição por tipo de acomodação. */
@Composable
fun GraficoRosca(fatias: List<Fatia>, modifier: Modifier = Modifier) {
    val p = LocalAppPalette.current
    val total = fatias.sumOf { it.valor }.takeIf { it > 0 } ?: 1.0
    val progresso = progressoDeEntrada(600, "rosca")

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(126.dp)) {
            val espessura = 26.dp.toPx()
            val raio = min(size.width, size.height) / 2 - espessura / 2
            val centro = Offset(size.width / 2, size.height / 2)
            var inicio = -90f
            fatias.forEach { f ->
                val varredura = (f.valor / total * 360.0).toFloat() * progresso
                drawArc(
                    color = f.cor,
                    startAngle = inicio + 0.6f,
                    sweepAngle = (varredura - 1.2f).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = Offset(centro.x - raio, centro.y - raio),
                    size = Size(raio * 2, raio * 2),
                    style = Stroke(width = espessura),
                )
                inicio += varredura
            }
        }
        Box(Modifier.size(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            fatias.forEach { f ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(f.cor))
                    Box(Modifier.size(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            f.rotulo,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(f.apoio, style = MaterialTheme.typography.labelSmall, color = p.faint)
                    }
                }
            }
        }
    }
}

// ============================================================== linha

/** Série anual em área — atividade por ano da última avaliação. */
@Composable
fun GraficoLinha(
    pontos: List<Pair<Int, Int>>,
    cor: Color,
    modifier: Modifier = Modifier,
    altura: androidx.compose.ui.unit.Dp = 130.dp,
) {
    val p = LocalAppPalette.current
    if (pontos.size < 2) {
        EstadoVazio("Sem série temporal", "São necessários ao menos dois anos com avaliações.")
        return
    }
    val maximo = pontos.maxOf { it.second }.coerceAtLeast(1)
    val progresso = progressoDeEntrada(600, "linha")

    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(altura)) {
            val passo = size.width / (pontos.size - 1)
            fun ponto(i: Int): Offset {
                val v = pontos[i].second / maximo.toFloat()
                return Offset(i * passo, size.height - v * size.height * progresso)
            }

            listOf(0f, 0.5f, 1f).forEach { f ->
                val y = size.height * f
                drawLine(p.line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }

            val area = Path().apply {
                moveTo(0f, size.height)
                for (i in pontos.indices) lineTo(ponto(i).x, ponto(i).y)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(area, cor.copy(alpha = 0.18f))

            val linha = Path().apply {
                moveTo(ponto(0).x, ponto(0).y)
                for (i in 1 until pontos.size) lineTo(ponto(i).x, ponto(i).y)
            }
            drawPath(linha, cor, style = Stroke(width = 2.5.dp.toPx()))

            for (i in pontos.indices) {
                drawCircle(cor, radius = 3.dp.toPx(), center = ponto(i))
            }
        }
        Box(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(pontos.first().first.toString(), style = MaterialTheme.typography.labelSmall, color = p.faint)
            Text(
                "pico: ${pontos.maxBy { it.second }.first}",
                style = MaterialTheme.typography.labelSmall,
                color = p.faint,
            )
            Text(pontos.last().first.toString(), style = MaterialTheme.typography.labelSmall, color = p.faint)
        }
    }
}

// ============================================================ boxplot

data class CaixaDados(
    val rotulo: String,
    val minimo: Double,
    val p25: Double,
    val mediana: Double,
    val p75: Double,
    val maximo: Double,
    val cor: Color,
)

/** Diagrama de caixa por cidade — substitui a dispersão do painel de desktop. */
@Composable
fun GraficoCaixa(
    caixas: List<CaixaDados>,
    modifier: Modifier = Modifier,
    formatarValor: (Double) -> String,
) {
    val p = LocalAppPalette.current
    if (caixas.isEmpty()) return
    val menor = caixas.minOf { it.minimo }
    val maior = caixas.maxOf { it.maximo }
    val amplitude = (maior - menor).takeIf { it > 0 } ?: 1.0

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        caixas.forEach { c ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(c.cor))
                    Box(Modifier.size(7.dp))
                    Text(
                        c.rotulo,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "mediana ${formatarValor(c.mediana)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = p.muted,
                    )
                }
                Box(Modifier.height(7.dp))
                Canvas(Modifier.fillMaxWidth().height(34.dp)) {
                    fun x(v: Double) = (((v - menor) / amplitude).toFloat()).coerceIn(0f, 1f) * size.width
                    val meio = size.height / 2

                    drawLine(
                        color = c.cor.copy(alpha = 0.45f),
                        start = Offset(x(c.minimo), meio),
                        end = Offset(x(c.maximo), meio),
                        strokeWidth = 2f,
                    )
                    listOf(c.minimo, c.maximo).forEach { v ->
                        drawLine(
                            color = c.cor.copy(alpha = 0.65f),
                            start = Offset(x(v), meio - 8.dp.toPx()),
                            end = Offset(x(v), meio + 8.dp.toPx()),
                            strokeWidth = 2f,
                        )
                    }
                    val esq = x(c.p25)
                    val dir = x(c.p75)
                    drawRoundRect(
                        color = c.cor.copy(alpha = 0.28f),
                        topLeft = Offset(esq, meio - 11.dp.toPx()),
                        size = Size(max(dir - esq, 2f), 22.dp.toPx()),
                        cornerRadius = CornerRadius(5f, 5f),
                    )
                    drawRoundRect(
                        color = c.cor,
                        topLeft = Offset(esq, meio - 11.dp.toPx()),
                        size = Size(max(dir - esq, 2f), 22.dp.toPx()),
                        cornerRadius = CornerRadius(5f, 5f),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                    drawLine(
                        color = c.cor,
                        start = Offset(x(c.mediana), meio - 12.dp.toPx()),
                        end = Offset(x(c.mediana), meio + 12.dp.toPx()),
                        strokeWidth = 3.dp.toPx(),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatarValor(c.p25), style = MaterialTheme.typography.labelSmall, color = p.faint)
                    Text(formatarValor(c.p75), style = MaterialTheme.typography.labelSmall, color = p.faint)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatarValor(menor), style = MaterialTheme.typography.labelSmall, color = p.faint)
            Text("escala comum", style = MaterialTheme.typography.labelSmall, color = p.faint)
            Text(formatarValor(maior), style = MaterialTheme.typography.labelSmall, color = p.faint)
        }
    }
}

// ============================================================ treemap

data class Celula(val rotulo: String, val apoio: String, val peso: Double, val intensidade: Double)

/**
 * Mapa de calor de bairros: a **área** de cada célula é o número de anúncios e a
 * **cor** é a métrica escolhida, na mesma escala sequencial de 7 classes do
 * coroplético do dashboard web. Substitui o mapa geográfico no celular, sem
 * exigir chave de API nem conexão.
 */
@Composable
fun MapaCalor(
    celulas: List<Celula>,
    modifier: Modifier = Modifier,
    altura: androidx.compose.ui.unit.Dp = 260.dp,
    onSelecionar: (Celula) -> Unit = {},
) {
    val p = LocalAppPalette.current
    val validas = celulas.filter { it.peso > 0 }.sortedByDescending { it.peso }
    if (validas.isEmpty()) {
        EstadoVazio("Sem bairros para exibir", "Ajuste os filtros para ver a distribuição.")
        return
    }
    val menor = validas.minOf { it.intensidade }
    val maior = validas.maxOf { it.intensidade }

    BoxWithConstraints(modifier.fillMaxWidth().height(altura)) {
        val larguraDp = maxWidth.value
        val alturaDp = maxHeight.value
        val retangulos = squarify(validas.map { it.peso }, Rect(0f, 0f, larguraDp, alturaDp))

        retangulos.forEachIndexed { i, r ->
            val celula = validas[i]
            val fracao = if (maior > menor) (celula.intensidade - menor) / (maior - menor) else 0.5
            val indiceCor = (fracao * (SequentialScale.size - 1)).toInt().coerceIn(0, SequentialScale.size - 1)
            val cor = SequentialScale[indiceCor]
            val cabe = r.width > 52f && r.height > 34f

            Box(
                Modifier
                    .offset(r.left.dp, r.top.dp)
                    .size(max(r.width - 2f, 1f).dp, max(r.height - 2f, 1f).dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(cor.copy(alpha = if (p.isDark) 0.88f else 0.92f))
                    .clickable { onSelecionar(celula) }
                    .padding(5.dp),
            ) {
                if (cabe) {
                    Column {
                        Text(
                            celula.rotulo,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            celula.apoio,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/** Faixa de legenda da escala sequencial. */
@Composable
fun LegendaEscala(rotuloMin: String, rotuloMax: String, modifier: Modifier = Modifier) {
    val p = LocalAppPalette.current
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(50))) {
            SequentialScale.forEach { c ->
                Box(Modifier.weight(1f).fillMaxSize().background(c))
            }
        }
        Box(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(rotuloMin, style = MaterialTheme.typography.labelSmall, color = p.faint)
            Text(rotuloMax, style = MaterialTheme.typography.labelSmall, color = p.faint)
        }
    }
}

/**
 * Treemap "squarified" (Bruls, Huizing & van Wijk, 2000): divide a área em
 * faixas escolhendo o corte que deixa os retângulos mais próximos de quadrados,
 * o que torna as áreas comparáveis a olho nu.
 */
private fun squarify(pesos: List<Double>, area: Rect): List<Rect> {
    val saida = MutableList(pesos.size) { Rect(0f, 0f, 0f, 0f) }
    var restante = pesos.sum()
    if (restante <= 0.0) return saida

    var livre = area
    var i = 0
    while (i < pesos.size && livre.width > 0.5f && livre.height > 0.5f) {
        val curto = min(livre.width, livre.height).toDouble()
        val escala = (livre.width * livre.height) / restante
        var somaFaixa = 0.0
        var melhor = Double.MAX_VALUE
        var ultimo = i
        var j = i

        while (j < pesos.size) {
            val nova = somaFaixa + pesos[j]
            val proporcao = pior(pesos.subList(i, j + 1), nova * escala, curto)
            if (proporcao > melhor) break
            melhor = proporcao
            somaFaixa = nova
            ultimo = j
            j++
        }

        val areaFaixa = somaFaixa * escala
        if (livre.width >= livre.height) {
            val larguraFaixa = (areaFaixa / livre.height).toFloat()
            var y = livre.top
            for (k in i..ultimo) {
                val alturaCelula = ((pesos[k] * escala) / larguraFaixa).toFloat()
                saida[k] = Rect(livre.left, y, livre.left + larguraFaixa, y + alturaCelula)
                y += alturaCelula
            }
            livre = Rect(livre.left + larguraFaixa, livre.top, livre.right, livre.bottom)
        } else {
            val alturaFaixa = (areaFaixa / livre.width).toFloat()
            var x = livre.left
            for (k in i..ultimo) {
                val larguraCelula = ((pesos[k] * escala) / alturaFaixa).toFloat()
                saida[k] = Rect(x, livre.top, x + larguraCelula, livre.top + alturaFaixa)
                x += larguraCelula
            }
            livre = Rect(livre.left, livre.top + alturaFaixa, livre.right, livre.bottom)
        }

        restante -= somaFaixa
        i = ultimo + 1
    }
    return saida
}

/** Pior razão de aspecto de uma faixa candidata — critério de parada do squarify. */
private fun pior(faixa: List<Double>, areaFaixa: Double, ladoCurto: Double): Double {
    if (areaFaixa <= 0.0 || ladoCurto <= 0.0) return Double.MAX_VALUE
    val maiorPeso = faixa.max()
    val menorPeso = faixa.min()
    val total = faixa.sum()
    if (total <= 0.0 || menorPeso <= 0.0) return Double.MAX_VALUE
    val escala = areaFaixa / total
    val s2 = ladoCurto * ladoCurto
    val a2 = areaFaixa * areaFaixa
    return max(s2 * (maiorPeso * escala) / a2, a2 / (s2 * (menorPeso * escala)))
}
