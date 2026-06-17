package com.example.fleetlogdb.network

/**
 * Constantes de la API REST del backend FleetLogAPI.
 * Cambia BASE_URL si tu servidor corre en otra IP/puerto.
 *
 * NOTA: Para emulador Android → usar 10.0.2.2 en lugar de localhost.
 * NOTA: Para dispositivo físico → usar la IP LAN de tu PC (ej. 192.168.1.X).
 */
object ApiConstants {
    // URL del servidor. Usando ngrok para que el OAuth de Google funcione.
    // Para volver al emulador sin Google OAuth: "http://10.0.2.2:3000"
    const val BASE_URL = "https://ribcage-flashily-crummiest.ngrok-free.dev"

    // --- Endpoints de Autenticación (Better-Auth) ---
    const val SIGN_UP        = "$BASE_URL/api/auth/sign-up/email"
    const val SIGN_IN        = "$BASE_URL/api/auth/sign-in/email"
    // Inicia el flujo OAuth de Google en el browser del dispositivo.
    // Este endpoint está fuera del wildcard de Better-Auth (/api/auth/*).
    const val SIGN_IN_GOOGLE = "$BASE_URL/api/google/start"

    // --- Endpoints de Vehículos ---
    const val VEHICLES           = "$BASE_URL/api/vehicles"
    fun vehicleById(id: Int)     = "$BASE_URL/api/vehicles/$id"

    // --- Endpoints de Conductores ---
    const val DRIVERS            = "$BASE_URL/api/drivers"
    fun driverById(id: Int)      = "$BASE_URL/api/drivers/$id"

    // --- Endpoints de Mantenimientos ---
    const val MAINTENANCE        = "$BASE_URL/api/maintenance"
    fun maintenanceById(id: Int) = "$BASE_URL/api/maintenance/$id"

    // --- Endpoints de Combustible ---
    const val FUEL               = "$BASE_URL/api/fuel"
    fun fuelById(id: Int)        = "$BASE_URL/api/fuel/$id"

    // Timeout en milisegundos para las conexiones HTTP
    const val CONNECT_TIMEOUT = 10_000
    const val READ_TIMEOUT    = 15_000
}
