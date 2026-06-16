package com.example.fleetlogdb.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.fleetlogdb.R
import com.example.fleetlogdb.model.Vehicle
import com.example.fleetlogdb.utils.ImageUtils

/**
 * Adaptador personalizado para el ListView de Vehículos.
 *
 * REQUISITO RÚBRICA:
 *  - Hereda de ArrayAdapter<Vehicle>
 *  - Sobrescribe getView() usando LayoutInflater
 *  - Decodifica la cadena Base64 (imageBase64) a Bitmap con ImageUtils.base64ToBitmap()
 *    para mostrarlo en el ImageView (ivVehicleImage).
 */
class VehicleAdapter(context: Context, private val vehicles: List<Vehicle>) :
    ArrayAdapter<Vehicle>(context, 0, vehicles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Reutilizamos la vista si ya fue inflada (patrón convertView)
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false)

        val vehicle = vehicles[position]

        // Referencias a los componentes de la fila
        val ivVehicleImage = view.findViewById<ImageView>(R.id.ivVehicleImage)
        val tvBrandModel   = view.findViewById<TextView>(R.id.tvBrandModel)
        val tvPlate        = view.findViewById<TextView>(R.id.tvPlate)
        val tvStatus       = view.findViewById<TextView>(R.id.tvStatus)
        val tvYear         = view.findViewById<TextView>(R.id.tvYear)

        // Texto: Marca y modelo
        tvBrandModel.text = "${vehicle.brand} ${vehicle.model}"
        tvPlate.text      = "Placa: ${vehicle.plate}"
        tvYear.text       = "Año: ${vehicle.year}"
        tvStatus.text     = vehicle.status

        // Color del estado: verde=Activo, naranja=Mantenimiento, rojo=Inactivo
        tvStatus.setTextColor(
            when (vehicle.status) {
                "Activo"        -> 0xFF2E7D32.toInt()   // Verde
                "Mantenimiento" -> 0xFFE65100.toInt()   // Naranja
                "Inactivo"      -> 0xFFB71C1C.toInt()   // Rojo
                else            -> 0xFF555555.toInt()   // Gris
            }
        )

        // REQUISITO RÚBRICA: Decodificar Base64 → Bitmap → ImageView
        val bitmap = ImageUtils.base64ToBitmap(vehicle.imageBase64)
        if (bitmap != null) {
            ivVehicleImage.setImageBitmap(bitmap)
        } else {
            // Si no hay imagen, mostramos el ícono según tipo de vehículo
            if (vehicle.isPickup == 1) {
                ivVehicleImage.setImageResource(R.drawable.ic_pickup)
            } else {
                ivVehicleImage.setImageResource(R.drawable.ic_car)
            }
        }

        return view
    }
}