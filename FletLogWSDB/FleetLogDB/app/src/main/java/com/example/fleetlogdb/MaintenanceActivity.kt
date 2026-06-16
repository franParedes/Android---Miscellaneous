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
import com.example.fleetlogdb.model.MaintenanceLog
import com.example.fleetlogdb.network.ApiConstants
import com.example.fleetlogdb.network.HttpTask
import com.example.fleetlogdb.ui.adapter.MaintenanceAdapter
import com.example.fleetlogdb.utils.SessionManager
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pantalla de Gestión de Mantenimientos – CRUD completo.
 * ENDPOINTS:
 *  GET    /api/maintenance       → lista con JOIN vehicles (incluye plate y brand)
 *  POST   /api/maintenance       → { vehicle_id, description, cost, service_date }
 *  PUT    /api/maintenance/:id   → { vehicle_id, description, cost, service_date }
 *  DELETE /api/maintenance/:id   → eliminar
 */
class MaintenanceActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var logsList: MutableList<MaintenanceLog>
    private lateinit var adapter: MaintenanceAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintenance)

        supportActionBar?.title = "Mantenimientos"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(this)
        listView = findViewById(R.id.listViewMaintenance)
        logsList = mutableListOf()
        adapter = MaintenanceAdapter(this, logsList)
        listView.adapter = adapter

        registerForContextMenu(listView)

        // Click corto → editar
        listView.setOnItemClickListener { _, _, position, _ ->
            showMaintenanceFormDialog(logsList[position])
        }

        loadMaintenance()
    }

    private fun loadMaintenance() {
        HttpTask("GET", ApiConstants.MAINTENANCE, null, sessionManager.getToken()) { result ->
            logsList.clear()
            if (result == null) {
                Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
                return@HttpTask
            }
            try {
                val arr = JSONArray(result)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    logsList.add(
                        MaintenanceLog(
                            id          = obj.getInt("id"),
                            vehicleId   = obj.getInt("vehicle_id"),
                            description = obj.getString("description"),
                            cost        = obj.getDouble("cost"),
                            serviceDate = obj.getString("service_date"),
                            plate       = obj.optString("plate", ""),
                            brand       = obj.optString("brand", "")
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
        menu?.add(0, 1, 0, "Registrar Mantenimiento")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            1 -> { showMaintenanceFormDialog(null); true }
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
            2 -> { showMaintenanceFormDialog(logsList[info.position]); true }
            3 -> { confirmDelete(logsList[info.position]); true }
            else -> super.onContextItemSelected(item)
        }
    }

    // --- FORMULARIO INSERT / UPDATE con validaciones ---
    private fun showMaintenanceFormDialog(logToEdit: MaintenanceLog?) {
        val dialogView    = layoutInflater.inflate(R.layout.dialog_maintenance_form, null)
        val etVehicleId   = dialogView.findViewById<EditText>(R.id.etVehicleId)
        val etDescription = dialogView.findViewById<EditText>(R.id.etMaintDescription)
        val etCost        = dialogView.findViewById<EditText>(R.id.etMaintCost)
        val etServiceDate = dialogView.findViewById<EditText>(R.id.etServiceDate)

        // Precarga datos si estamos editando
        if (logToEdit != null) {
            etVehicleId.setText(logToEdit.vehicleId.toString())
            etDescription.setText(logToEdit.description)
            etCost.setText(logToEdit.cost.toString())
            etServiceDate.setText(logToEdit.serviceDate)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (logToEdit == null) "Registrar Mantenimiento" else "Editar Mantenimiento")
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val vehicleIdStr = etVehicleId.text.toString().trim()
                val description  = etDescription.text.toString().trim()
                val costStr      = etCost.text.toString().trim()
                val serviceDate  = etServiceDate.text.toString().trim()

                // Validaciones
                when {
                    vehicleIdStr.isEmpty() -> { etVehicleId.error = "El ID del vehículo es obligatorio"; return@setOnClickListener }
                    vehicleIdStr.toIntOrNull() == null -> { etVehicleId.error = "Debe ser un número entero"; return@setOnClickListener }
                    description.isEmpty() -> { etDescription.error = "La descripción es obligatoria"; return@setOnClickListener }
                    costStr.isEmpty() -> { etCost.error = "El costo es obligatorio"; return@setOnClickListener }
                    costStr.toDoubleOrNull() == null -> { etCost.error = "Debe ser un número válido"; return@setOnClickListener }
                    serviceDate.isEmpty() -> { etServiceDate.error = "La fecha de servicio es obligatoria"; return@setOnClickListener }
                }

                val bodyJson = JSONObject().apply {
                    put("vehicle_id",   vehicleIdStr.toInt())
                    put("description",  description)
                    put("cost",         costStr.toDouble())
                    put("service_date", serviceDate)
                }.toString()

                if (logToEdit == null) {
                    HttpTask("POST", ApiConstants.MAINTENANCE, bodyJson, sessionManager.getToken()) { r ->
                        Toast.makeText(this,
                            if (r != null) "Mantenimiento registrado ✔" else "Error",
                            Toast.LENGTH_SHORT).show()
                        loadMaintenance()
                    }.execute()
                } else {
                    HttpTask("PUT", ApiConstants.maintenanceById(logToEdit.id), bodyJson, sessionManager.getToken()) { r ->
                        Toast.makeText(this,
                            if (r != null) "Mantenimiento actualizado ✔" else "Error al actualizar",
                            Toast.LENGTH_SHORT).show()
                        loadMaintenance()
                    }.execute()
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmDelete(log: MaintenanceLog) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Mantenimiento")
            .setMessage("¿Eliminar el mantenimiento: '${log.description}'?")
            .setPositiveButton("Sí") { _, _ ->
                HttpTask("DELETE", ApiConstants.maintenanceById(log.id), null, sessionManager.getToken()) { r ->
                    Toast.makeText(this,
                        if (r != null) "Eliminado" else "Error",
                        Toast.LENGTH_SHORT).show()
                    loadMaintenance()
                }.execute()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
