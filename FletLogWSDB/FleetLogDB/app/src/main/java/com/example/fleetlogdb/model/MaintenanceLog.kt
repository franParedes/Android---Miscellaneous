package com.example.fleetlogdb.model

/**
 * POJO que representa la tabla 'maintenance_logs' del backend.
 * El GET /api/maintenance hace JOIN con vehicles, por eso incluye 'plate' y 'brand'.
 */
data class MaintenanceLog(
    var id: Int = 0,
    var vehicleId: Int = 0,           // Mapeado desde 'vehicle_id'
    var description: String = "",
    var cost: Double = 0.0,
    var serviceDate: String = "",     // Mapeado desde 'service_date', formato: "YYYY-MM-DD"
    // Campos extra del JOIN con vehicles
    var plate: String = "",
    var brand: String = ""
)
