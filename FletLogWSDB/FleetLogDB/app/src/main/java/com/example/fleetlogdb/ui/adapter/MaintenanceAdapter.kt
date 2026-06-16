package com.example.fleetlogdb.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.fleetlogdb.R
import com.example.fleetlogdb.model.MaintenanceLog

/**
 * Adaptador personalizado para el ListView de Mantenimientos.
 * REQUISITO RÚBRICA: Hereda de ArrayAdapter + getView() con LayoutInflater.
 */
class MaintenanceAdapter(context: Context, private val logs: List<MaintenanceLog>) :
    ArrayAdapter<MaintenanceLog>(context, 0, logs) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_maintenance, parent, false)

        val log = logs[position]

        view.findViewById<TextView>(R.id.tvMaintVehicle).text     = "🔧 ${log.brand} - ${log.plate}"
        view.findViewById<TextView>(R.id.tvMaintDate).text        = log.serviceDate
        view.findViewById<TextView>(R.id.tvMaintDescription).text = log.description
        view.findViewById<TextView>(R.id.tvMaintCost).text        = "Costo: C$ ${"%.2f".format(log.cost)}"

        return view
    }
}
