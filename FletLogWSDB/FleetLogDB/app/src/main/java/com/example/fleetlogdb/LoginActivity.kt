package com.example.fleetlogdb

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fleetlogdb.network.ApiConstants
import com.example.fleetlogdb.network.HttpTask
import com.example.fleetlogdb.utils.SessionManager
import org.json.JSONObject

/**
 * Pantalla de Login y Registro.
 * Consume la API Better-Auth usando HttpTask (AsyncTask + HttpURLConnection).
 *
 * REQUISITO RÚBRICA:
 *  - Guarda el token en SharedPreferences via SessionManager.
 *  - Usa JSONObject nativo (no Gson/Moshi) para parsear la respuesta.
 *  - Usa AlertDialog.Builder para mostrar errores.
 *
 * FLUJO DE BETTER-AUTH:
 *  - POST /api/auth/sign-in/email → body: { email, password }
 *    Respuesta: JSON con token en { token: "..." }
 *  - POST /api/auth/sign-up/email → body: { email, password, name }
 *    Respuesta similar al sign-in
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnGoogle: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // Si ya hay sesión activa, saltamos directamente al MainActivity
        if (sessionManager.isLoggedIn()) {
            goToMain()
            return
        }

        // Referencias a las vistas
        etEmail      = findViewById(R.id.etEmail)
        etPassword   = findViewById(R.id.etPassword)
        btnLogin     = findViewById(R.id.btnLogin)
        btnRegister  = findViewById(R.id.btnRegister)
        btnGoogle    = findViewById(R.id.btnGoogle)
        progressBar  = findViewById(R.id.progressBar)

        // --- BOTÓN: Iniciar Sesión ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!validarCampos(email, password)) return@setOnClickListener

            showLoading(true)
            performSignIn(email, password)
        }

        // --- BOTÓN: Registrarse ---
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!validarCampos(email, password)) return@setOnClickListener

            // Para el registro, el 'name' es la parte del email antes del '@'
            val name = email.substringBefore("@")

            showLoading(true)
            performSignUp(email, password, name)
        }

        // --- BOTÓN: Google (login social con OAuth) ---
        // El flujo completo es:
        //   1. Android abre la URL del backend en el navegador del sistema
        //   2. El backend redirige a Google OAuth
        //   3. Google autentica al usuario y devuelve control al backend
        //   4. El backend redirige a fleetlog://auth?token=...
        //   5. Android intercepta el deep link en AuthCallbackActivity
        btnGoogle.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ApiConstants.SIGN_IN_GOOGLE))
            startActivity(intent)
        }
    }

    /**
     * Llama a POST /api/auth/sign-in/email
     * REQUISITO: AsyncTask con HttpURLConnection
     */
    private fun performSignIn(email: String, password: String) {
        // Construimos el body JSON con clases nativas (JSONObject)
        val bodyJson = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString()

        HttpTask(
            method   = "POST",
            url      = ApiConstants.SIGN_IN,
            body     = bodyJson,
            token    = null,
            callback = { result ->
                showLoading(false)
                handleAuthResponse(result, email)
            }
        ).execute()
    }

    /**
     * Llama a POST /api/auth/sign-up/email
     */
    private fun performSignUp(email: String, password: String, name: String) {
        val bodyJson = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("name", name)
        }.toString()

        HttpTask(
            method   = "POST",
            url      = ApiConstants.SIGN_UP,
            body     = bodyJson,
            token    = null,
            callback = { result ->
                showLoading(false)
                handleAuthResponse(result, email)
            }
        ).execute()
    }

    /**
     * Parsea la respuesta de autenticación de Better-Auth usando JSONObject nativo.
     * REQUISITO RÚBRICA: Parseo con JSONObject/JSONArray nativo (sin Gson/Moshi).
     *
     * Better-Auth devuelve: { "token": "...", "user": { ... } }
     */
    private fun handleAuthResponse(result: String?, email: String) {
        if (result == null) {
            mostrarError("Error de conexión. Verifica que el servidor esté corriendo.")
            return
        }

        try {
            val json = JSONObject(result)

            // Intentamos extraer el token del campo "token"
            val token = json.optString("token", "")

            if (token.isNotEmpty()) {
                // Guardamos el token en SharedPreferences
                sessionManager.saveSession(token, email)
                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                goToMain()
            } else {
                // La respuesta tiene un campo "message" o "error" con el detalle
                val errorMsg = json.optString("message",
                    json.optString("error", "Credenciales incorrectas"))
                mostrarError(errorMsg)
            }

        } catch (e: Exception) {
            mostrarError("Error al procesar la respuesta del servidor.")
        }
    }

    /** Valida que los campos no estén vacíos. */
    private fun validarCampos(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor llena todos los campos.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /** Muestra u oculta el ProgressBar y habilita/deshabilita los botones. */
    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled    = !loading
        btnRegister.isEnabled = !loading
        btnGoogle.isEnabled   = !loading
    }

    /** Muestra un error con AlertDialog.Builder.
     *  REQUISITO RÚBRICA: Uso obligatorio de AlertDialog.Builder. */
    private fun mostrarError(mensaje: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Error de Autenticación")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    /** Navega al MainActivity y cierra el LoginActivity. */
    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
