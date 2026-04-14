package com.example.greetingcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greetingcard.ui.theme.GreetingCardTheme

class MainActivity : ComponentActivity() {
    // onCreate es el punto de entrada a las apps Android
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Define el diseño a través de funciones de compatibilidad
        // Todas las funciones marcadas con @Composable se pueden llamar
        // desde setContent
        setContent {
            GreetingCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    /* 
        Surface es un contenedor que representa una sección de la UI a la que se le puede
        modificar el aspecto como el borde o color de fondo
    */
    Surface(color = Color.Cyan) {
        Text(
            text = "Hi my name is $name!",
            modifier = modifier.padding(24.dp)
        )
    }
}

// Permite ver el aspecto de la función de componibilidad sin tener que compilar la app
// Para activar el preview se deben agregar las anotaciones @Composable y @Preview
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GreetingCardTheme {
        Greeting("Francisco")
    }
}