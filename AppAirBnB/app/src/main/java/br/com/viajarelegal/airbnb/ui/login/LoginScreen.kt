package br.com.viajarelegal.airbnb.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.viajarelegal.airbnb.data.Auth
import br.com.viajarelegal.airbnb.ui.theme.Brand
import br.com.viajarelegal.airbnb.ui.theme.Brand2
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    temaEscuro: Boolean,
    onAlternarTema: () -> Unit,
    onEntrar: () -> Unit,
) {
    val p = LocalAppPalette.current
    val teclado = LocalSoftwareKeyboardController.current

    var usuario by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    fun tentar() {
        teclado?.hide()
        when (val r = Auth.entrar(usuario, senha)) {
            is Auth.Resultado.Ok -> { erro = null; onEntrar() }
            is Auth.Resultado.Erro -> erro = r.mensagem
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Faixa decorativa no topo, com o gradiente da marca.
        Box(
            Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Brand.copy(alpha = if (p.isDark) 0.22f else 0.16f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        )

        IconButton(
            onClick = onAlternarTema,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp),
        ) {
            Icon(
                if (temaEscuro) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Alternar tema",
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                // A faixa colorida sangra ate o topo; o conteudo respeita as
                // barras do sistema, obrigatorio no modo borda a borda do
                // Android 15, onde a tela de login nao tem Scaffold para isso.
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.height(64.dp))

            Box(
                Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(listOf(Brand, Brand2)),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("🏠", style = MaterialTheme.typography.displaySmall)
            }

            Box(Modifier.height(18.dp))
            Text(
                "Viajar é Legal",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Painel de preços e demanda de locação",
                style = MaterialTheme.typography.bodyMedium,
                color = p.muted,
                textAlign = TextAlign.Center,
            )

            Box(Modifier.height(30.dp))

            val cores = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = p.line,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )

            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it; erro = null },
                label = { Text("Usuário") },
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                colors = cores,
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = p.muted) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                isError = erro != null,
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
            )

            Box(Modifier.height(12.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it; erro = null },
                label = { Text("Senha") },
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                colors = cores,
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = p.muted) },
                trailingIcon = {
                    IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                        Icon(
                            if (mostrarSenha) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (mostrarSenha) "Ocultar senha" else "Mostrar senha",
                            tint = p.muted,
                        )
                    }
                },
                visualTransformation = if (mostrarSenha) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { tentar() }),
                isError = erro != null,
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
            )

            AnimatedVisibility(visible = erro != null) {
                Text(
                    erro.orEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Box(Modifier.height(20.dp))

            Button(
                onClick = { tentar() },
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp).height(52.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Entrar", style = MaterialTheme.typography.titleMedium)
            }

            Box(Modifier.height(22.dp))

            // Ambiente de demonstração: as credenciais ficam à vista de propósito.
            Row(
                Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "AMBIENTE DE DEMONSTRAÇÃO",
                        style = MaterialTheme.typography.labelSmall,
                        color = p.faint,
                    )
                    Box(Modifier.height(3.dp))
                    Text(
                        "usuário  dante   ·   senha  dante123",
                        style = MaterialTheme.typography.bodySmall,
                        color = p.muted,
                    )
                }
            }

            Box(Modifier.height(30.dp))
            Text(
                "${Auth.NOME_EXIBICAO} · ${Auth.EMAIL}",
                style = MaterialTheme.typography.labelSmall,
                color = p.faint,
            )
            Box(Modifier.height(28.dp))
        }
    }
}
