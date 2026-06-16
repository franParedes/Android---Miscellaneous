package com.example.fleetlogdb.model

/**
 * POJO que representa la tabla 'drivers' del backend.
 * Los campos coinciden con el JSON devuelto por GET /api/drivers.
 */
data class Driver(
    var id: Int = 0,
    var name: String = "",
    var licenseNumber: String = "",   // Mapeado desde 'license_number' en el JSON
    var phone: String = ""
)
