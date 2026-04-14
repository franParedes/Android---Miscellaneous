package com.example.fleetlog.data

import com.example.fleetlog.model.Vehicle

/*
* En Kotlin, cuando declaras algo como object en lugar de class, estás creando un Singleton.
* Esto significa que solo existirá una única instancia de VehicleRepository en toda la aplicación.
* Cualquier pantalla que pida VehicleRepository.getVehicles() verá exactamente la misma lista.
* */
object VehicleRepository {
    // La lista real es PRIVADA y mutable (se puede modificar).
    private val vehiclesList = mutableListOf<Vehicle>()

    fun getVehicles(): List<Vehicle> {
        return vehiclesList.toList()
    }

    fun addVehicle(vehicle: Vehicle) {
        vehiclesList.add(vehicle)
    }

    fun updateVehicle(updatedVehicle: Vehicle) {
        val index = vehiclesList.indexOfFirst { it.id == updatedVehicle.id }
        if (index != -1) {
            vehiclesList[index] = updatedVehicle
        }
    }

    fun deleteVehicle(id: Int) {
        vehiclesList.removeAll { it.id == id }
    }
}