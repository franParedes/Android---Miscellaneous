package com.example.fleetlogdb.model

/**
 * POJO que representa la tabla 'fuel_logs' del backend.
 * El GET /api/fuel hace JOIN con vehicles, por eso incluye 'plate' y 'brand'.
 */
data class FuelLog(
    var id: Int = 0,
    var vehicleId: Int = 0,           // Mapeado desde 'vehicle_id'
    var gallons: Double = 0.0,
    var totalCost: Double = 0.0,      // Mapeado desde 'total_cost'
    var dateFilled: String = "",      // Mapeado desde 'date_filled', formato: "YYYY-MM-DD"
    // Campos extra del JOIN con vehicles
    var plate: String = "",
    var brand: String = ""
)
