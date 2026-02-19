package com.trycatchers.hotel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.trycatchers.hotel.compose.HotelPereMariaApp
import com.trycatchers.hotel.ui.theme.HotelPereMariaTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Punto de entrada de la app Android.
 * Inicializa Compose, configura edge-to-edge y aplica el tema dinámico de la sesión.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Crea el árbol principal de UI y registra el contenedor de navegación Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            var darkTheme by rememberSaveable { mutableStateOf(isDarkTheme) }
            HotelPereMariaTheme(darkTheme = darkTheme) {
                HotelPereMariaApp(onThemeToggle = { darkTheme = !darkTheme })
            }
        }
    }
}
