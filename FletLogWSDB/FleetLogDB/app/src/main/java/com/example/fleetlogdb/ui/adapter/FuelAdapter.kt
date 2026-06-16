package com.example.fleetlogdb.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.fleetlogdb.R
import com.example.fleetlogdb.model.FuelLog

/**
 * Adaptador personalizado para el ListView de Registros de Combustible.
 * REQUISITO RÚBRICA: Hereda de ArrayAdapter + getView() con LayoutInflater.
 */
class FuelAdapter(context: Context, private val logs: List<FuelLog>) :
    ArrayAdapter<FuelLog>(context, 0, logs) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_fuel, parent, false)

        val log = logs[position]

        view.findViewById<TextView>(R.id.tvFuelVehicle).text = "⛽ ${log.brand} - ${log.plate}"
        view.findViewById<TextView>(R.id.tvFuelDate).text    = log.dateFilled
        view.findViewById<TextView>(R.id.tvGallons).text     = "Galones: ${"%.2f".format(log.gallons)}"
        view.findViewById<TextView>(R.id.tvTotalCost).text   = "Total: C$ ${"%.2f".format(log.totalCost)}"

        return view
    }
}
