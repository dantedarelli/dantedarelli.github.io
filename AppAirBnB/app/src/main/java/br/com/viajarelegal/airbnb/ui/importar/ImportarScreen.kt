package br.com.viajarelegal.airbnb.ui.importar

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.viajarelegal.airbnb.data.CsvParser
import br.com.viajarelegal.airbnb.data.Importer
import br.com.viajarelegal.airbnb.data.Listing
import br.com.viajarelegal.airbnb.data.ListingRepository
import br.com.viajarelegal.airbnb.data.Origem
import br.com.viajarelegal.airbnb.domain.Format
import br.com.viajarelegal.airbnb.ui.DashboardViewModel
import br.com.viajarelegal.airbnb.ui.components.Distintivo
import br.com.viajarelegal.airbnb.ui.components.Painel
import br.com.viajarelegal.airbnb.ui.components.TituloSecao
import br.com.viajarelegal.airbnb.ui.theme.Brand
import br.com.viajarelegal.airbnb.ui.theme.Brand2
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette

/**
 * Tela de importação: uma planilha inteira ou um único anúncio digitado.
 *
 * Tudo entra na base em memória do [ListingRepository] — não há banco de dados,
 * então o que for importado vale enquanto o aplicativo estiver aberto.
 */
@Composable
fun ImportarScreen(vm: DashboardViewModel, onConcluir: () -> Unit) {
    var aba by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = aba,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Tab(
                selected = aba == 0,
                onClick = { aba = 0 },
                text = { Text("Planilha") },
            )
            Tab(
                selected = aba == 1,
                onClick = { aba = 1 },
                text = { Text("Linha manual") },
            )
        }

        when (aba) {
            0 -> AbaPlanilha(vm, onConcluir)
            else -> AbaManual(vm, onConcluir)
        }
    }
}

// ==================================================================== planilha

private data class ArquivoLido(
    val nome: String,
    val tabela: CsvParser.Tabela,
    val separador: Char,
)

@Composable
private fun AbaPlanilha(vm: DashboardViewModel, onConcluir: () -> Unit) {
    val p = LocalAppPalette.current
    val contexto = LocalContext.current

    var arquivo by remember { mutableStateOf<ArquivoLido?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    val mapa = remember { mutableStateMapOf<Importer.Campo, Int>() }
    var cidadePadrao by remember { mutableStateOf("") }
    var moedaPadrao by remember { mutableStateOf("BRL") }

    val seletor = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val bytes = contexto.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: ByteArray(0)
            // Planilhas exportadas do Excel em pt-BR costumam vir em Latin-1;
            // o caractere de substituicao denuncia a decodificacao errada.
            val comoUtf8 = bytes.toString(Charsets.UTF_8)
            val texto = if (comoUtf8.contains('�')) bytes.toString(Charsets.ISO_8859_1) else comoUtf8

            if (texto.isBlank()) {
                erro = "O arquivo está vazio."
                return@rememberLauncherForActivityResult
            }

            val separador = CsvParser.detectarSeparador(texto)
            val tabela = CsvParser.parse(texto, separador)
            if (tabela.cabecalho.isEmpty() || tabela.vazia) {
                erro = "Não encontrei linhas de dados abaixo do cabeçalho."
                return@rememberLauncherForActivityResult
            }

            val nome = nomeDoArquivo(contexto, uri)
            arquivo = ArquivoLido(nome, tabela, separador)
            mapa.clear()
            mapa.putAll(Importer.detectar(tabela.cabecalho))
            cidadePadrao = nome.substringBeforeLast(".").take(24)
            erro = null
        } catch (e: Exception) {
            erro = "Não consegui ler o arquivo: ${e.message ?: "formato não reconhecido"}."
        }
    }

    val lido = arquivo
    val faltando = if (lido == null) emptyList() else Importer.faltamObrigatorios(mapa)
    // A construcao percorre a planilha inteira: so refaz quando algo que a afeta muda.
    val assinaturaMapa = mapa.entries.sortedBy { it.key.name }.joinToString { "${it.key}=${it.value}" }
    val previa = remember(lido, assinaturaMapa, cidadePadrao, moedaPadrao) {
        if (lido == null || Importer.faltamObrigatorios(mapa).isNotEmpty()) null
        else Importer.construir(
            tabela = lido.tabela,
            mapa = mapa.toMap(),
            cidadePadrao = cidadePadrao.ifBlank { "IMPORTADA" },
            moedaPadrao = moedaPadrao.ifBlank { "BRL" },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.dp,
                        if (lido != null) Brand2 else p.line,
                        RoundedCornerShape(16.dp),
                    )
                    .clickable {
                        seletor.launch(
                            arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*"),
                        )
                    }
                    .padding(vertical = 26.dp, horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (lido != null) Icons.Filled.Description else Icons.Filled.UploadFile,
                        contentDescription = null,
                        tint = if (lido != null) Brand2 else p.muted,
                        modifier = Modifier.size(34.dp),
                    )
                    Box(Modifier.height(9.dp))
                    Text(
                        lido?.nome ?: "Escolher planilha CSV",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(Modifier.height(3.dp))
                    Text(
                        if (lido != null)
                            "${Format.inteiro(lido.tabela.linhas.size)} linhas · " +
                                "${lido.tabela.cabecalho.size} colunas · separador \"${lido.separador}\""
                        else "vírgula, ponto e vírgula ou tabulação · toque para procurar",
                        style = MaterialTheme.typography.bodySmall,
                        color = p.muted,
                    )
                }
            }
        }

        if (erro != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                    Box(Modifier.size(8.dp))
                    Text(
                        erro.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        if (lido != null) {

            item {
                Painel(
                    titulo = "Identificação da base",
                    apoio = "usado quando a planilha não traz colunas de cidade e moeda",
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cidadePadrao,
                            onValueChange = { cidadePadrao = it },
                            label = { Text("Cidade") },
                            singleLine = true,
                            shape = RoundedCornerShape(11.dp),
                            modifier = Modifier.weight(1.6f),
                        )
                        OutlinedTextField(
                            value = moedaPadrao,
                            onValueChange = { moedaPadrao = it.uppercase().take(4) },
                            label = { Text("Moeda") },
                            singleLine = true,
                            shape = RoundedCornerShape(11.dp),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                Painel(
                    titulo = "Mapeamento de colunas",
                    apoio = "o que foi reconhecido sozinho já vem preenchido",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Importer.Campo.entries.forEach { campo ->
                            SeletorDeColuna(
                                campo = campo,
                                cabecalho = lido.tabela.cabecalho,
                                indiceAtual = mapa[campo] ?: -1,
                                onEscolher = { indice ->
                                    if (indice < 0) mapa.remove(campo) else mapa[campo] = indice
                                },
                            )
                        }
                    }
                }
            }

            item {
                Painel(titulo = "Prévia das primeiras linhas") {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        lido.tabela.cabecalho.forEachIndexed { i, coluna ->
                            Column(Modifier.width(112.dp), horizontalAlignment = Alignment.Start) {
                                Text(
                                    coluna,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = p.faint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Box(Modifier.height(5.dp))
                                lido.tabela.linhas.take(4).forEach { linha ->
                                    Text(
                                        linha.getOrElse(i) { "" }.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Painel(titulo = "Resultado da leitura") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when {
                            faltando.isNotEmpty() -> Linha(
                                icone = Icons.Filled.ErrorOutline,
                                cor = MaterialTheme.colorScheme.error,
                                texto = "Indique a coluna de ${faltando.joinToString { it.rotulo.lowercase() }} " +
                                    "para prosseguir.",
                            )
                            previa == null || previa.listings.isEmpty() -> Linha(
                                icone = Icons.Filled.ErrorOutline,
                                cor = MaterialTheme.colorScheme.error,
                                texto = previa?.motivo ?: "Nada a importar.",
                            )
                            else -> {
                                Linha(
                                    icone = Icons.Filled.CheckCircle,
                                    cor = Brand2,
                                    texto = "${Format.inteiro(previa.listings.size)} anúncios prontos para entrar na base.",
                                )
                                if (previa.ignoradas > 0) {
                                    Text(
                                        "${Format.inteiro(previa.ignoradas)} linhas ficaram de fora por " +
                                            "não terem preço ou bairro válidos.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = p.muted,
                                    )
                                }
                                val cidades = previa.listings.map { it.cidade }.distinct()
                                Text(
                                    "Cidades: ${cidades.joinToString()} · moedas: " +
                                        previa.listings.map { it.moeda }.distinct().joinToString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = p.muted,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { arquivo = null; mapa.clear(); erro = null },
                        modifier = Modifier.weight(1f),
                    ) { Text("Descartar") }
                    Button(
                        onClick = {
                            val r = previa ?: return@Button
                            vm.acrescentar(r.listings)
                            vm.aviso = "${Format.inteiro(r.listings.size)} anúncios importados de ${lido.nome}."
                            onConcluir()
                        },
                        enabled = previa != null && previa.listings.isNotEmpty(),
                        modifier = Modifier.weight(1.4f),
                    ) { Text("Importar para a base") }
                }
            }
        }

        item {
            Text(
                "A leitura acontece toda no aparelho: nada é enviado para lugar nenhum, " +
                    "e a base volta ao estado original ao fechar o aplicativo.",
                style = MaterialTheme.typography.bodySmall,
                color = p.faint,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeletorDeColuna(
    campo: Importer.Campo,
    cabecalho: List<String>,
    indiceAtual: Int,
    onEscolher: (Int) -> Unit,
) {
    val p = LocalAppPalette.current
    var aberto by remember { mutableStateOf(false) }
    val naoMapeado = indiceAtual < 0

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(campo.rotulo, style = MaterialTheme.typography.bodyMedium)
                if (campo.obrigatorio) {
                    Box(Modifier.size(6.dp))
                    Distintivo("OBRIGATÓRIO", if (naoMapeado) MaterialTheme.colorScheme.error else Brand2)
                }
            }
        }
        Box {
            Row(
                Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.dp,
                        if (campo.obrigatorio && naoMapeado) MaterialTheme.colorScheme.error else p.line,
                        RoundedCornerShape(9.dp),
                    )
                    .clickable { aberto = true }
                    .padding(horizontal = 11.dp, vertical = 8.dp)
                    .widthIn(max = 148.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (naoMapeado) "não usar" else cabecalho.getOrElse(indiceAtual) { "?" },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (naoMapeado) p.faint else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                DropdownMenuItem(
                    text = { Text("não usar", color = p.faint) },
                    onClick = { onEscolher(-1); aberto = false },
                )
                cabecalho.forEachIndexed { i, coluna ->
                    DropdownMenuItem(
                        text = { Text(coluna.ifBlank { "coluna ${i + 1}" }) },
                        onClick = { onEscolher(i); aberto = false },
                    )
                }
            }
        }
    }
}

// ====================================================================== manual

@Composable
private fun AbaManual(vm: DashboardViewModel, onConcluir: () -> Unit) {
    val p = LocalAppPalette.current

    var cidade by remember { mutableStateOf(vm.facetas.cidades.firstOrNull() ?: "RJ") }
    var moeda by remember { mutableStateOf("BRL") }
    var nome by remember { mutableStateOf("") }
    var anfitriao by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var regiao by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Entire home/apt") }
    var preco by remember { mutableStateOf("") }
    var noites by remember { mutableStateOf("1") }
    var avaliacoes by remember { mutableStateOf("0") }
    var disponibilidade by remember { mutableStateOf("365") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var tentouSalvar by remember { mutableStateOf(false) }

    val precoValor = CsvParser.numero(preco)
    val erroPreco = tentouSalvar && (precoValor == null || precoValor <= 0)
    val erroBairro = tentouSalvar && bairro.isBlank()
    val erroCidade = tentouSalvar && cidade.isBlank()
    val valido = precoValor != null && precoValor > 0 && bairro.isNotBlank() && cidade.isNotBlank()

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Painel(
                titulo = "Novo anúncio",
                apoio = "entra na base na hora e passa a valer nos gráficos",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    TituloSecao("Identificação")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Campo(
                            valor = cidade,
                            onValueChange = { cidade = it },
                            rotulo = "Cidade *",
                            erro = erroCidade,
                            modifier = Modifier.weight(1.6f),
                        )
                        Campo(
                            valor = moeda,
                            onValueChange = { moeda = it.uppercase().take(4) },
                            rotulo = "Moeda",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Campo(nome, { nome = it }, "Nome do anúncio")
                    Campo(anfitriao, { anfitriao = it }, "Anfitrião")

                    TituloSecao("Localização")
                    Campo(bairro, { bairro = it }, "Bairro *", erro = erroBairro)
                    Campo(regiao, { regiao = it }, "Região / borough (opcional)")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Campo(
                            latitude, { latitude = it }, "Latitude",
                            teclado = KeyboardType.Decimal, modifier = Modifier.weight(1f),
                        )
                        Campo(
                            longitude, { longitude = it }, "Longitude",
                            teclado = KeyboardType.Decimal, modifier = Modifier.weight(1f),
                        )
                    }

                    TituloSecao("Oferta")
                    Campo(tipo, { tipo = it }, "Tipo de acomodação")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Campo(
                            preco, { preco = it }, "Preço / noite *",
                            teclado = KeyboardType.Decimal, erro = erroPreco,
                            modifier = Modifier.weight(1.3f),
                        )
                        Campo(
                            noites, { noites = it }, "Noites mín.",
                            teclado = KeyboardType.Number, modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Campo(
                            avaliacoes, { avaliacoes = it }, "Avaliações",
                            teclado = KeyboardType.Number, modifier = Modifier.weight(1f),
                        )
                        Campo(
                            disponibilidade, { disponibilidade = it }, "Disponibilidade (dias)",
                            teclado = KeyboardType.Number, modifier = Modifier.weight(1.3f),
                        )
                    }

                    if (tentouSalvar && !valido) {
                        Text(
                            "Cidade, bairro e um preço maior que zero são obrigatórios.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onConcluir, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        tentouSalvar = true
                        if (!valido) return@Button
                        val novo = Listing(
                            id = ListingRepository.proximoIdManual(),
                            cidade = cidade.trim(),
                            moeda = moeda.trim().ifBlank { "BRL" },
                            nome = nome.trim().ifBlank { "Anúncio sem título" },
                            anfitriao = anfitriao.trim().ifBlank { "—" },
                            regiao = regiao.trim(),
                            bairro = bairro.trim(),
                            tipo = tipo.trim().ifBlank { "Não informado" },
                            latitude = CsvParser.numero(latitude) ?: 0.0,
                            longitude = CsvParser.numero(longitude) ?: 0.0,
                            preco = precoValor ?: 0.0,
                            noitesMinimas = CsvParser.inteiro(noites)?.coerceAtLeast(1) ?: 1,
                            avaliacoes = CsvParser.inteiro(avaliacoes)?.coerceAtLeast(0) ?: 0,
                            avaliacoesPorMes = 0.0,
                            ultimaAvaliacao = "",
                            disponibilidade365 = CsvParser.inteiro(disponibilidade)?.coerceIn(0, 365) ?: 0,
                            anunciosDoAnfitriao = 1,
                            origem = Origem.MANUAL,
                        )
                        vm.acrescentar(listOf(novo))
                        vm.aviso = "Anúncio \"${novo.nome}\" adicionado em ${novo.cidade}."
                        onConcluir()
                    },
                    modifier = Modifier.weight(1.4f),
                ) { Text("Adicionar à base") }
            }
        }

        item {
            Text(
                "Campos marcados com * são obrigatórios. O preço entra na moeda " +
                    "informada e é convertido pela taxa da aba Comparar.",
                style = MaterialTheme.typography.bodySmall,
                color = p.faint,
            )
        }
    }
}

// ===================================================================== apoio

@Composable
private fun Campo(
    valor: String,
    onValueChange: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    erro: Boolean = false,
) {
    val p = LocalAppPalette.current
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(rotulo) },
        singleLine = true,
        isError = erro,
        shape = RoundedCornerShape(11.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = p.line),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun Linha(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    cor: Color,
    texto: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(18.dp))
        Box(Modifier.size(8.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = cor,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** Nome amigavel do arquivo escolhido, via provedor de documentos do sistema. */
private fun nomeDoArquivo(contexto: Context, uri: Uri): String {
    contexto.contentResolver
        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val coluna = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (coluna >= 0 && cursor.moveToFirst()) {
                cursor.getString(coluna)?.takeIf { it.isNotBlank() }?.let { return it }
            }
        }
    return uri.lastPathSegment?.substringAfterLast('/') ?: "planilha.csv"
}
