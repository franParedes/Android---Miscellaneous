package com.example.fleetlogdb.model

/**
 * POJO que representa la tabla 'vehicles' del backend.
 * Los campos coinciden exactamente con el JSON devuelto por GET /api/vehicles.
 */
data class Vehicle(
    var id: Int = 0,
    var brand: String = "",
    var model: String = "",
    var plate: String = "",
    var year: Int = 0,
    var color: String = "Blanco",
    var mileage: Int = 0,
    var status: String = "Activo",   // "Activo", "Mantenimiento", "Inactivo"
    var imageBase64: String? = null,  // Cadena Base64 de la imagen del vehículo
    var isPickup: Int = 0             // 1 = Sí, 0 = No
)
