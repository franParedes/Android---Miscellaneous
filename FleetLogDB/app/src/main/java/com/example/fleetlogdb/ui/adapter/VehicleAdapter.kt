package com.example.fleetlogdb.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.fleetlogdb.model.Vehicle

class VehicleAdapter(context: Context, private val vehicles: List<Vehicle>):
    ArrayAdapter<Vehicle>(context, 0, vehicles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false)
        }

        var vehicle = vehicles[position]

        // Referencias a los componentes de la vista
        val tvBrandModel = view!!.findViewById<TextView>(R.id.tvBrandModel)
        val tvPlate = view.findViewById<TextView>(R.id.tvPlate)
        val tvYear = view.findViewById<TextView>(R.id.tvYear)
        val ivIcon = view.findViewById<ImageView>(R.id.ivVehicleIcon)

        tvBrandModel.text = "${vehicle.brand} ${vehicle.model}"
        tvPlate.text = "Placa: ${vehicle.plate}"
        tvYear.text = "Año: ${vehicle.year}"

        if (vehicle.isPickup == 1) {
            ivIcon.setImageResource(R.drawable.ic_pickup)
        } else {
            ivIcon.setImageResource(R.drawable.ic_car)
        }

        return view
    }
}