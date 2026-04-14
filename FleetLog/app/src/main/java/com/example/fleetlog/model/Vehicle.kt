package com.example.fleetlog.model

data class Vehicle(
    val id: Int,
    val brand: String,
    var model: String,
    var plate: String, // Placa
    var year: Int, // Año de fabricación
    var isPickup: Boolean
)
