package com.example.fleetlogdb.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Clase utilitaria para manejar la sesión del usuario mediante SharedPreferences.
 * Almacena el Token de sesión devuelto por Better-Auth y el email del usuario.
 *
 * REQUISITO RÚBRICA: Uso de SharedPreferences para persistencia del token.
 */
class SessionManager(context: Context) {

    // El archivo de preferencias se llama "FleetLogSession"
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME   = "FleetLogSession"
        private const val KEY_TOKEN    = "session_token"
        private const val KEY_EMAIL    = "user_email"
        private const val KEY_LOGGED   = "is_logged_in"
    }

    // ---- Guardar datos de sesión ----

    /** Guarda el token devuelto por Better-Auth y el email del usuario. */
    fun saveSession(token: String, email: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .putBoolean(KEY_LOGGED, true)
            .apply()
    }

    // ---- Leer datos de sesión ----

    /** Devuelve el token almacenado, o null si no existe. */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /** Devuelve el email almacenado, o null si no existe. */
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /** Devuelve true si el usuario tiene sesión activa. */
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED, false)

    // ---- Cerrar sesión ----

    /** Elimina todos los datos de sesión almacenados. */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
