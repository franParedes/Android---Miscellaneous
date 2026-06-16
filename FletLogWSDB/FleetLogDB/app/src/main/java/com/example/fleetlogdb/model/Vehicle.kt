package com.example.fleetlogdb.model

data class Vehicle(
    var id: Int = 0,
    var brand: String,
    var model: String,
    var plate: String,
    var year: Int,
    var isPickup: Int // 1 = Si, 0 = No
)
