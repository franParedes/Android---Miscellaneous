package com.example.temperatureconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temperatureconverter.ui.theme.TemperatureConverterTheme
import kotlin.math.roundToInt

enum class ConversionDirection {
    CELSIUS_TO_FAHRENHEIT,
    FAHRENHEIT_TO_CELSIUS
}

// MainActivity = Pantalla principal de la aplicación
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class) // Para quitar el aviso de API experimental en TopAppBar
    override fun onCreate(savedInstanceState: Bundle?) { // Es el primer metodo que se ejecuta cuando se abre la app

        // savedInstanceState = Estado de la aplicación (si se cerró o se abrió)
        super.onCreate(savedInstanceState) // Llama al metodo padre ComponentActivity para que haga su configuración interna

        // Permite que la app ocupe toda la pantalla incluyendo los botones y la barra de estado
        enableEdgeToEdge()

        setContent { // Define lo que se va a mostrar en pantalla
            TemperatureConverterTheme { // Tema visual

                // Da la estructura base de la aplicación
                Scaffold(
                    modifier = Modifier.fillMaxSize(), // Hace que ocupe to-do el espacio disponible
                    topBar = { // Define que va en la parte superior de la pantalla
                        TopAppBar( // Es la barra superior
                            title = { Text("Convertidor de temperatura") },
                            colors = TopAppBarDefaults.mediumTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                ) { innerPadding -> // Es el espacio necesario para que el contenido no se superponga a la barra
                    // El modifier define como se posiciona y dimensiona
                    TemperatureConverterScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TemperatureConverterScreen(modifier: Modifier = Modifier) {
    // Estos imports son los que arreglan el error del "getValue/setValue"
    // import androidx.compose.runtime.getValue
    // import androidx.compose.runtime.setValue
    var inputValue by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(ConversionDirection.CELSIUS_TO_FAHRENHEIT) }
    var resultText by remember { mutableStateOf("") }

    fun performConversion() {
        if (inputValue.isBlank() || inputValue == "-") {
            resultText = ""
            return
        }
        val number = inputValue.toDoubleOrNull()
        if (number == null) {
            resultText = "Entrada inválida"
            return
        }

        val converted = when (direction) {
            ConversionDirection.CELSIUS_TO_FAHRENHEIT -> (number * 9 / 5) + 32
            ConversionDirection.FAHRENHEIT_TO_CELSIUS -> (number - 32) * 5 / 9
        }

        val rounded = (converted * 100).roundToInt() / 100.0

        resultText = when (direction) {
            ConversionDirection.CELSIUS_TO_FAHRENHEIT -> "$number °C = $rounded °F"
            ConversionDirection.FAHRENHEIT_TO_CELSIUS -> "$number °F = $rounded °C"
        }
    }

    // Lanzar la conversión automáticamente cuando cambien los valores
    LaunchedEffect(inputValue, direction) {
        performConversion()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Ajuste sus valores",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Convertir:", style = MaterialTheme.typography.titleMedium)

                // Opción C a F
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = direction == ConversionDirection.CELSIUS_TO_FAHRENHEIT,
                        onClick = { direction = ConversionDirection.CELSIUS_TO_FAHRENHEIT }
                    )
                    Text("De Celsius a Fahrenheit")
                }

                // Opción F a C
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = direction == ConversionDirection.FAHRENHEIT_TO_CELSIUS,
                        onClick = { direction = ConversionDirection.FAHRENHEIT_TO_CELSIUS }
                    )
                    Text("De Fahrenheit a Celsius")
                }
            }
        }

        OutlinedTextField(
            value = inputValue,
            onValueChange = { newValue ->
                // Permite números, punto decimal y signo negativo al inicio
                if (newValue.isEmpty() || newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                    inputValue = newValue
                }
            },
            label = { Text("Cantidad a convertir") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { performConversion() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Convertir")
        }

        if (resultText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = resultText,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTemperatureConverter() {
    TemperatureConverterTheme {
        TemperatureConverterScreen()
    }
}