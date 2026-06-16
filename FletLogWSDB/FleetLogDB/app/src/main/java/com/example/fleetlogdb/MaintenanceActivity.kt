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
 * Pantalla de Gestión de Mantenimientos.
 * ENDPOINTS:
 *  GET  /api/maintenance      → lista con JOIN vehicles (incluye plate y brand)
 *  POST /api/maintenance      → { vehicle_id, description, cost, service_date }
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
            1 -> { showMaintenanceFormDialog(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("Opciones")
        menu?.add(0, 2, 0, "Eliminar")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        if (item.itemId == 2) {
            confirmDelete(logsList[info.position])
            return true
        }
        return super.onContextItemSelected(item)
    }

    private fun showMaintenanceFormDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_maintenance_form, null)
        val etVehicleId   = dialogView.findViewById<EditText>(R.id.etVehicleId)
        val etDescription = dialogView.findViewById<EditText>(R.id.etMaintDescription)
        val etCost        = dialogView.findViewById<EditText>(R.id.etMaintCost)
        val etServiceDate = dialogView.findViewById<EditText>(R.id.etServiceDate)

        AlertDialog.Builder(this)
            .setTitle("Registrar Mantenimiento")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val bodyJson = JSONObject().apply {
                    put("vehicle_id",   etVehicleId.text.toString().toIntOrNull() ?: 0)
                    put("description",  etDescription.text.toString())
                    put("cost",         etCost.text.toString().toDoubleOrNull() ?: 0.0)
                    put("service_date", etServiceDate.text.toString())
                }.toString()

                HttpTask("POST", ApiConstants.MAINTENANCE, bodyJson, sessionManager.getToken()) { r ->
                    Toast.makeText(this,
                        if (r != null) "Mantenimiento registrado ✔" else "Error",
                        Toast.LENGTH_SHORT).show()
                    loadMaintenance()
                }.execute()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
