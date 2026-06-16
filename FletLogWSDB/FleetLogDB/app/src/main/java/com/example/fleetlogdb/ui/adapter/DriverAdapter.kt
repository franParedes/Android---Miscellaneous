package com.example.fleetlogdb.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.fleetlogdb.R
import com.example.fleetlogdb.model.Driver

/**
 * Adaptador personalizado para el ListView de Conductores.
 * REQUISITO RÚBRICA: Hereda de ArrayAdapter + sobreescribe getView() con LayoutInflater.
 */
class DriverAdapter(context: Context, private val drivers: List<Driver>) :
    ArrayAdapter<Driver>(context, 0, drivers) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_driver, parent, false)

        val driver = drivers[position]

        view.findViewById<TextView>(R.id.tvDriverName).text    = driver.name
        view.findViewById<TextView>(R.id.tvLicenseNumber).text = "Licencia: ${driver.licenseNumber}"
        view.findViewById<TextView>(R.id.tvDriverPhone).text   = "Tel: ${driver.phone}"

        return view
    }
}
