package com.example.fleetlogdb.network

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * AsyncTask genérico para realizar llamadas HTTP a la REST API.
 *
 * REQUISITO RÚBRICA:
 *  - Hereda de AsyncTask (prohibido Retrofit, Volley, Ktor, Coroutines)
 *  - Usa exclusivamente HttpURLConnection para la conexión de red
 *
 * Uso:
 *   HttpTask(
 *       method   = "GET",           // "GET", "POST", "PUT", "DELETE"
 *       url      = ApiConstants.VEHICLES,
 *       body     = null,            // JSONObject como String para POST/PUT
 *       token    = sessionManager.getToken(),
 *       callback = { result -> ... }  // result = respuesta HTTP como String, o null si falló
 *   ).execute()
 */
@Suppress("DEPRECATION")   // AsyncTask está deprecado en API 30+ pero es requerido por la rúbrica
class HttpTask(
    private val method: String,
    private val url: String,
    private val body: String? = null,
    private val token: String? = null,
    private val callback: (result: String?) -> Unit
) : AsyncTask<Void, Void, String?>() {

    /**
     * Se ejecuta en el HILO DE FONDO (background thread).
     * Realiza la conexión HTTP y devuelve la respuesta como String.
     */
    override fun doInBackground(vararg params: Void?): String? {
        var connection: HttpURLConnection? = null
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.connectTimeout = ApiConstants.CONNECT_TIMEOUT
            connection.readTimeout = ApiConstants.READ_TIMEOUT

            // --- Encabezados comunes ---
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json")

            // --- Token de sesión Better-Auth (si existe) ---
            // Better-Auth usa cookie de sesión: se envía en el header Cookie
            if (!token.isNullOrEmpty()) {
                connection.setRequestProperty("Cookie", "better-auth.session_token=$token")
            }

            // --- Envío del body (para POST y PUT) ---
            if (body != null && (method == "POST" || method == "PUT")) {
                connection.doOutput = true
                val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
                writer.write(body)
                writer.flush()
                writer.close()
            }

            // --- Lectura de la respuesta ---
            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            response.toString()

        } catch (e: Exception) {
            e.printStackTrace()
            null  // Devolvemos null para indicar error de red
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Se ejecuta en el HILO PRINCIPAL (UI thread) cuando doInBackground termina.
     * Invoca el callback con el resultado.
     */
    override fun onPostExecute(result: String?) {
        callback(result)
    }
}
