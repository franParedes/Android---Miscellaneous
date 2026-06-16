package com.example.fleetlogdb

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fleetlogdb.model.FuelLog
import com.example.fleetlogdb.network.ApiConstants
import com.example.fleetlogdb.network.HttpTask
import com.example.fleetlogdb.ui.adapter.FuelAdapter
import com.example.fleetlogdb.utils.SessionManager
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pantalla de Gestión de Registros de Combustible – CRUD completo.
 * ENDPOINTS:
 *  GET    /api/fuel       → lista con JOIN vehicles (incluye plate y brand)
 *  POST   /api/fuel       → { vehicle_id, gallons, total_cost, date_filled }
 *  PUT    /api/fuel/:id   → { vehicle_id, gallons, total_cost, date_filled }
 *  DELETE /api/fuel/:id   → eliminar
 */
class FuelActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var fuelList: MutableList<FuelLog>
    private lateinit var adapter: FuelAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel)

        supportActionBar?.title = "Combustible"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(this)
        listView = findViewById(R.id.listViewFuel)
        fuelList = mutableListOf()
        adapter = FuelAdapter(this, fuelList)
        listView.adapter = adapter

        registerForContextMenu(listView)

        // Click corto → editar
        listView.setOnItemClickListener { _, _, position, _ ->
            showFuelFormDialog(fuelList[position])
        }

        loadFuel()
    }

    private fun loadFuel() {
        HttpTask("GET", ApiConstants.FUEL, null, sessionManager.getToken()) { result ->
            fuelList.clear()
            if (result == null) {
                Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
                return@HttpTask
            }
            try {
                val arr = JSONArray(result)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    fuelList.add(
                        FuelLog(
                            id         = obj.getInt("id"),
                            vehicleId  = obj.getInt("vehicle_id"),
                            gallons    = obj.getDouble("gallons"),
                            totalCost  = obj.getDouble("total_cost"),
                            dateFilled = obj.getString("date_filled"),
                            plate      = obj.optString("plate", ""),
                            brand      = obj.optString("brand", "")
                        )
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al parsear", Toast.LENGTH_SHORT).show()
            }
            adapter.notifyDataSetChanged()
        }.execute()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "Registrar Combustible")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            1 -> { showFuelFormDialog(null); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("Opciones")
        menu?.add(0, 2, 0, "Editar")
        menu?.add(0, 3, 0, "Eliminar")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        return when (item.itemId) {
            2 -> { showFuelFormDialog(fuelList[info.position]); true }
            3 -> { confirmDelete(fuelList[info.position]); true }
            else -> super.onContextItemSelected(item)
        }
    }

    // --- FORMULARIO INSERT / UPDATE con validaciones ---
    private fun showFuelFormDialog(logToEdit: FuelLog?) {
        val dialogView   = layoutInflater.inflate(R.layout.dialog_fuel_form, null)
        val etVehicleId  = dialogView.findViewById<EditText>(R.id.etFuelVehicleId)
        val etGallons    = dialogView.findViewById<EditText>(R.id.etGallons)
        val etTotalCost  = dialogView.findViewById<EditText>(R.id.etTotalCost)
        val etDateFilled = dialogView.findViewById<EditText>(R.id.etDateFilled)

        // Precarga datos si estamos editando
        if (logToEdit != null) {
            etVehicleId.setText(logToEdit.vehicleId.toString())
            etGallons.setText(logToEdit.gallons.toString())
            etTotalCost.setText(logToEdit.totalCost.toString())
            etDateFilled.setText(logToEdit.dateFilled)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (logToEdit == null) "Registrar Combustible" else "Editar Combustible")
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val vehicleIdStr = etVehicleId.text.toString().trim()
                val gallonsStr   = etGallons.text.toString().trim()
                val costStr      = etTotalCost.text.toString().trim()
                val date         = etDateFilled.text.toString().trim()

                // Validaciones
                when {
                    vehicleIdStr.isEmpty() -> { etVehicleId.error = "El ID del vehículo es obligatorio"; return@setOnClickListener }
                    vehicleIdStr.toIntOrNull() == null -> { etVehicleId.error = "Debe ser un número entero"; return@setOnClickListener }
                    gallonsStr.isEmpty() -> { etGallons.error = "Los galones son obligatorios"; return@setOnClickListener }
                    gallonsStr.toDoubleOrNull() == null -> { etGallons.error = "Debe ser un número válido"; return@setOnClickListener }
                    costStr.isEmpty() -> { etTotalCost.error = "El costo total es obligatorio"; return@setOnClickListener }
                    costStr.toDoubleOrNull() == null -> { etTotalCost.error = "Debe ser un número válido"; return@setOnClickListener }
                    date.isEmpty() -> { etDateFilled.error = "La fecha es obligatoria"; return@setOnClickListener }
                }

                val bodyJson = JSONObject().apply {
                    put("vehicle_id",  vehicleIdStr.toInt())
                    put("gallons",     gallonsStr.toDouble())
                    put("total_cost",  costStr.toDouble())
                    put("date_filled", date)
                }.toString()

                if (logToEdit == null) {
                    HttpTask("POST", ApiConstants.FUEL, bodyJson, sessionManager.getToken()) { r ->
                        Toast.makeText(this,
                            if (r != null) "Combustible registrado ✔" else "Error",
                            Toast.LENGTH_SHORT).show()
                        loadFuel()
                    }.execute()
                } else {
                    HttpTask("PUT", ApiConstants.fuelById(logToEdit.id), bodyJson, sessionManager.getToken()) { r ->
                        Toast.makeText(this,
                            if (r != null) "Registro actualizado ✔" else "Error al actualizar",
                            Toast.LENGTH_SHORT).show()
                        loadFuel()
                    }.execute()
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmDelete(log: FuelLog) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Registro")
            .setMessage("¿Eliminar el registro del ${log.brand} - ${log.plate} (${log.dateFilled})?")
            .setPositiveButton("Sí") { _, _ ->
                HttpTask("DELETE", ApiConstants.fuelById(log.id), null, sessionManager.getToken()) { r ->
                    Toast.makeText(this,
                        if (r != null) "Eliminado" else "Error",
                        Toast.LENGTH_SHORT).show()
                    loadFuel()
                }.execute()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
