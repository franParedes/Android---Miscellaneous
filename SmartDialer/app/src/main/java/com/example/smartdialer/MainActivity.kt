package com.example.smartdialer

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var tvScreen: TextView
    private val CALL_PERMISSION_CODE = 25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvScreen = findViewById(R.id.tvScreen)

        setupButtons()

        // Manejar el intent entrante (si otra app nos llama usando ACTION_DIAL)
        handleIncomingIntent()
    }

    private fun setupButtons() {
        // Agrupamos los botones numéricos para asignarles la misma acción de tipeo
        val numericButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
            R.id.btn8, R.id.btn9, R.id.btnStar, R.id.btnHash
        )

        for (id in numericButtons) {
            findViewById< Button>(id).setOnClickListener { view ->
                val b = view as Button
                // Solo tomamos el primer caracter por si el texto del botón dice "0\n(+)"
                val digit = b.text.toString().substring(0, 1)
                tvScreen.append(digit)
                invalidateOptionsMenu()
            }
        }

        // Eventos especiales (clicks largos)
        val btn0 = findViewById<Button>(R.id.btn0)
        btn0.setOnLongClickListener {
            val currentText = tvScreen.text.toString()
            if (!currentText.startsWith("+")) {
                tvScreen.text = "+$currentText"
                invalidateOptionsMenu()
            }
            true // Consumimos el evento
        }

        val btn1 = findViewById<Button>(R.id.btn1)
        btn1.setOnLongClickListener {
            // Llama al buzón de voz predeterminado
            makeCall("voicemail:")
            true
        }

        // Botón Borrar (C)
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            val currentText = tvScreen.text.toString()
            if (currentText.isNotEmpty()) {
                tvScreen.text = currentText.dropLast(1)
                invalidateOptionsMenu()
            }
        }

        // Botón Llamar
        findViewById<Button>(R.id.btnCall).setOnClickListener {
            val number = tvScreen.text.toString()
            if (number.isNotEmpty()) {
                makeCall("tel:$number")
            } else {
                Toast.makeText(this, "Ingrese un número", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- MENU DE OPCIONES ---
    // Este metodo se ejecuta cada vez que llamamos a invalidateOptionsMenu()
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val number = tvScreen.text.toString()

        // Buscamos el item de "Añadir a contactos"
        val addContactItem = menu?.findItem(R.id.action_add_contact)

        // El botón solo será visible si la pantalla NO está vacía
        addContactItem?.isVisible = number.isNotEmpty()

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dialer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val number = tvScreen.text.toString()

        return when (item.itemId) {
            R.id.action_sms -> {
                if (number.isNotEmpty()) {
                    val uri = Uri.parse("smsto:$number")
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    startActivity(intent)
                }
                true
            }
            R.id.action_add_contact -> {
                val uri = Uri.parse("content://contacts/people")
                val intent = Intent(Intent.ACTION_INSERT, uri)

                // Le pasamos el número a la app de contactos para que lo pre-llene
                intent.putExtra("phone", number)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --- MANEJO DE LLAMADAS Y PERMISOS ---
    private fun makeCall(uriString: String) {
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_CALL, uri) // ACTION_CALL Realiza la llamada

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), CALL_PERMISSION_CODE)
        } else {
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALL_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario concedió el permiso, llamamos al número en pantalla
                val number = tvScreen.text.toString()
                if (number.isNotEmpty()) {
                    val uri = Uri.parse("tel:$number")
                    val intent = Intent(Intent.ACTION_CALL, uri)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Permiso denegado para realizar llamadas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Si abren nuestra app desde otra app (ej. un link de teléfono en el navegador)
    private fun handleIncomingIntent() {
        if (intent.action == Intent.ACTION_DIAL) {
            val data: Uri? = intent.data
            if (data != null) {
                // Extraemos el número de la URI entrante y lo ponemos en la pantalla
                val schemeSpecificPart = data.schemeSpecificPart
                tvScreen.text = schemeSpecificPart
            }
        }
    }
}