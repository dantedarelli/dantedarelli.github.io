package br.com.viajarelegal.airbnb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import br.com.viajarelegal.airbnb.ui.AppRaiz
import br.com.viajarelegal.airbnb.ui.theme.ViajarELegalTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // O tema segue o sistema até o usuário escolher manualmente no cabeçalho.
            val sistemaEscuro = isSystemInDarkTheme()
            var escolha by remember { mutableStateOf<Boolean?>(null) }
            val escuro = escolha ?: sistemaEscuro

            ViajarELegalTheme(darkTheme = escuro) {
                AppRaiz(
                    temaEscuro = escuro,
                    onAlternarTema = { escolha = !escuro },
                )
            }
        }
    }
}
