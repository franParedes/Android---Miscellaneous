package com.example.fleetlogdb.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Utilidades para conversión de imágenes entre Bitmap, URI y cadenas Base64.
 *
 * REQUISITO RÚBRICA:
 *  - La imagen seleccionada de la galería se convierte a Base64 para enviarla en el JSON.
 *  - La cadena Base64 que llega del JSON se decodifica a Bitmap para mostrarse en ImageView.
 */
object ImageUtils {

    /**
     * Convierte una URI de imagen (obtenida al abrir la galería) a una cadena Base64.
     *
     * @param context Contexto de la actividad.
     * @param uri URI de la imagen seleccionada.
     * @param maxWidthPx Ancho máximo para redimensionar (evita strings enormes). Default: 800px.
     * @return Cadena Base64 de la imagen, o null si ocurre un error.
     */
    fun uriToBase64(context: Context, uri: Uri, maxWidthPx: Int = 800): String? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            // Primero decodificamos solo las dimensiones (sin cargar en memoria)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculamos el factor de escala para no exceder maxWidthPx
            val sampleSize = calculateInSampleSize(options, maxWidthPx)

            // Ahora decodificamos con escala reducida
            val inputStream2 = context.contentResolver.openInputStream(uri) ?: return null
            val scaledOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, scaledOptions)
            inputStream2.close()

            bitmap?.let { bitmapToBase64(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte un Bitmap a una cadena Base64 (formato JPEG, calidad 80%).
     *
     * @param bitmap El bitmap a convertir.
     * @return Cadena Base64 del bitmap.
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Convierte una cadena Base64 (recibida del JSON del backend) a un Bitmap.
     * Usado en el getView() del adaptador para mostrar la imagen en el ImageView.
     *
     * @param base64String La cadena Base64 a decodificar.
     * @return Bitmap decodificado, o null si la cadena es inválida o nula.
     */
    fun base64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrEmpty()) return null
        return try {
            val byteArray = Base64.decode(base64String, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Calcula el factor de sub-muestreo para redimensionar la imagen.
     * Evita carga innecesaria de memoria.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int): Int {
        val width = options.outWidth
        var inSampleSize = 1
        if (width > reqWidth) {
            val halfWidth = width / 2
            while ((halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
