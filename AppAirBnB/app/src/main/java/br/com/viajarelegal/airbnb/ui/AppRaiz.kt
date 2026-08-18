package br.com.viajarelegal.airbnb.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.viajarelegal.airbnb.data.Auth
import br.com.viajarelegal.airbnb.ui.anuncios.AnunciosScreen
import br.com.viajarelegal.airbnb.ui.bairros.BairrosScreen
import br.com.viajarelegal.airbnb.ui.comparar.CompararScreen
import br.com.viajarelegal.airbnb.ui.filtros.FiltrosSheet
import br.com.viajarelegal.airbnb.ui.importar.ImportarScreen
import br.com.viajarelegal.airbnb.ui.login.LoginScreen
import br.com.viajarelegal.airbnb.ui.painel.PainelScreen
import br.com.viajarelegal.airbnb.ui.theme.Brand
import br.com.viajarelegal.airbnb.ui.theme.LocalAppPalette

private enum class Aba(val rota: String, val rotulo: String, val icone: ImageVector) {
    PAINEL("painel", "Painel", Icons.Filled.BarChart),
    BAIRROS("bairros", "Bairros", Icons.Filled.GridView),
    COMPARAR("comparar", "Comparar", Icons.Filled.CompareArrows),
    ANUNCIOS("anuncios", "Anúncios", Icons.Filled.ListAlt),
}

private const val ROTA_IMPORTAR = "importar"

@Composable
fun AppRaiz(temaEscuro: Boolean, onAlternarTema: () -> Unit) {
    var autenticado by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !autenticado,
            exit = fadeOut() + slideOutVertically { -it / 6 },
        ) {
            LoginScreen(
                temaEscuro = temaEscuro,
                onAlternarTema = onAlternarTema,
                onEntrar = { autenticado = true },
            )
        }

        AnimatedVisibility(
            visible = autenticado,
            enter = fadeIn() + slideInVertically { it / 8 },
        ) {
            AppAutenticado(
                temaEscuro = temaEscuro,
                onAlternarTema = onAlternarTema,
                onSair = { autenticado = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppAutenticado(
    temaEscuro: Boolean,
    onAlternarTema: () -> Unit,
    onSair: () -> Unit,
) {
    val vm: DashboardViewModel = viewModel()
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rotaAtual = entrada?.destination?.route
    val emImportacao = rotaAtual == ROTA_IMPORTAR

    val snackbar = remember { SnackbarHostState() }
    var filtrosAbertos by remember { mutableStateOf(false) }

    LaunchedEffect(vm.aviso) {
        val mensagem = vm.aviso ?: return@LaunchedEffect
        vm.aviso = null
        snackbar.showSnackbar(mensagem)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Brand.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🏠", style = MaterialTheme.typography.bodyMedium)
                        }
                        Box(Modifier.size(10.dp))
                        Column {
                            Text(
                                if (emImportacao) "Importar dados" else "Dashboard AirBnB",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                if (emImportacao) "planilha ou lançamento manual" else "Viajar é Legal · ${Auth.NOME_EXIBICAO}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalAppPalette.current.faint,
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (emImportacao) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    if (!emImportacao) {
                        IconButton(onClick = { filtrosAbertos = true }) {
                            Icon(Icons.Filled.Tune, contentDescription = "Filtros")
                        }
                    }
                    IconButton(onClick = onAlternarTema) {
                        Icon(
                            if (temaEscuro) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Alternar tema",
                        )
                    }
                    IconButton(onClick = onSair) {
                        Icon(Icons.Filled.Logout, contentDescription = "Sair")
                    }
                },
            )
        },
        bottomBar = {
            if (!emImportacao) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Aba.entries.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = {
                                nav.navigate(aba.rota) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(aba.icone, contentDescription = aba.rotulo) },
                            label = { Text(aba.rotulo, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                unselectedIconColor = LocalAppPalette.current.muted,
                                unselectedTextColor = LocalAppPalette.current.muted,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!emImportacao) {
                ExtendedFloatingActionButton(
                    onClick = { nav.navigate(ROTA_IMPORTAR) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Filled.FileUpload, contentDescription = null) },
                    text = { Text("Importar") },
                )
            }
        },
    ) { interno ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(interno),
            verticalArrangement = Arrangement.Top,
        ) {
            NavHost(navController = nav, startDestination = Aba.PAINEL.rota) {
                composable(Aba.PAINEL.rota) { PainelScreen(vm) }
                composable(Aba.BAIRROS.rota) { BairrosScreen(vm) }
                composable(Aba.COMPARAR.rota) { CompararScreen(vm) }
                composable(Aba.ANUNCIOS.rota) { AnunciosScreen(vm) }
                composable(ROTA_IMPORTAR) {
                    ImportarScreen(
                        vm = vm,
                        onConcluir = { nav.popBackStack() },
                    )
                }
            }
        }
    }

    if (filtrosAbertos) {
        FiltrosSheet(vm = vm, onFechar = { filtrosAbertos = false })
    }
}
