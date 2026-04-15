package com.example.fleetlog.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fleetlog.R
import com.example.fleetlog.model.Vehicle

// El adaptador recibe la lista de datos y dos acciones (lambdas) para manejar los clics
class VehicleAdapter(
    private var vehiclesList: List<Vehicle>,
    private val onItemClicked: (Vehicle) -> Unit,             // Para ver/editar el registro
    private val onItemLongClicked: (View, Vehicle) -> Unit    // Para lanzar el menú contextual
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    // El ViewHolder es una clase interna que "sostiene" las vistas de nuestro item_vehicle.xml
    class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivVehicleIcon)
        val tvBrandModel: TextView = view.findViewById(R.id.tvBrandModel)
        val tvPlate: TextView = view.findViewById(R.id.tvPlate)
        val tvYear: TextView = view.findViewById(R.id.tvYear)
    }

    // onCreateViewHolder "infla" el XML por cada fila visible en la pantalla
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    // onBindViewHolder enlaza los datos del objeto Vehicle con las vistas del XML
    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehiclesList[position]

        // Asignamos los textos
        holder.tvBrandModel.text = "${vehicle.brand} ${vehicle.model}"
        holder.tvPlate.text = "Placa: ${vehicle.plate}"
        holder.tvYear.text = "Año: ${vehicle.year}"

        // Lógica visual: Si es pickup, mostramos un ícono; si no, el otro.
        // (Asegúrate de que los nombres ic_pickup e ic_car coincidan con los que creaste)
        if (vehicle.isPickup) {
            holder.ivIcon.setImageResource(R.drawable.ic_pickup)
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_car)
        }

        // --- MANEJO DE EVENTOS (Cumpliendo requisitos) ---

        // Clic corto: Para "Permitir ver un registro / editar"
        holder.itemView.setOnClickListener {
            onItemClicked(vehicle)
        }

        // Clic largo: Para lanzar el "Menú contextual" (Eliminar/Editar)
        holder.itemView.setOnLongClickListener { view ->
            onItemLongClicked(view, vehicle)
            true // Retornamos true para indicar que consumimos el evento de clic largo
        }
    }

    // getItemCount le dice a la lista cuántos elementos hay en total
    override fun getItemCount(): Int {
        return vehiclesList.size
    }

    // Función auxiliar para actualizar la lista cuando agreguemos, editemos o eliminemos datos
    fun updateData(newVehiclesList: List<Vehicle>) {
        vehiclesList = newVehiclesList
        notifyDataSetChanged() // Notifica al adaptador que los datos cambiaron para que redibuje
    }
}