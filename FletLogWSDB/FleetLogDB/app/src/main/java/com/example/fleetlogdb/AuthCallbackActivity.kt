package com.example.fleetlogdb

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fleetlogdb.utils.SessionManager

/**
 * Activity invisible que intercepta el Deep Link "fleetlog://auth"
 * cuando Google redirige de vuelta a la aplicación tras el login OAuth.
 *
 * El backend redirige a: fleetlog://auth?token=XXXXX&email=user@gmail.com
 *
 * Android lo intercepta aquí gracias al intent-filter definido en el Manifest.
 * Extraemos el token de la URI y lo guardamos en SharedPreferences.
 */
class AuthCallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data

        if (data != null && data.scheme == "fleetlog" && data.host == "auth") {
            // Revisamos si hubo un error en el flujo OAuth
            val error = data.getQueryParameter("error")
            if (!error.isNullOrEmpty()) {
                Toast.makeText(this, "Error al iniciar sesión con Google: $error", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }

            // Extraemos los parámetros de la URL de callback
            val token = data.getQueryParameter("token")
            val email = data.getQueryParameter("email") ?: "usuario@google.com"

            val sessionManager = SessionManager(this)

            if (!token.isNullOrEmpty()) {
                sessionManager.saveSession(token, email)
                Toast.makeText(this, "¡Bienvenido con Google!", Toast.LENGTH_SHORT).show()
                // Navegamos al MainActivity limpiando el back stack
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error al iniciar sesión con Google.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        } else {
            // Si no hay datos válidos, regresamos al Login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
