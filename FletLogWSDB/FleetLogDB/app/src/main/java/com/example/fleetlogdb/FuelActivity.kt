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
 * Pantalla de Gestión de Registros de Combustible.
 * ENDPOINTS:
 *  GET    /api/fuel       → lista con JOIN vehicles (incluye plate y brand)
 *  POST   /api/fuel       → { vehicle_id, gallons, total_cost, date_filled }
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
            1 -> { showFuelFormDialog(); true }
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
            confirmDelete(fuelList[info.position])
            return true
        }
        return super.onContextItemSelected(item)
    }

    private fun showFuelFormDialog() {
        val dialogView   = layoutInflater.inflate(R.layout.dialog_fuel_form, null)
        val etVehicleId  = dialogView.findViewById<EditText>(R.id.etFuelVehicleId)
        val etGallons    = dialogView.findViewById<EditText>(R.id.etGallons)
        val etTotalCost  = dialogView.findViewById<EditText>(R.id.etTotalCost)
        val etDateFilled = dialogView.findViewById<EditText>(R.id.etDateFilled)

        AlertDialog.Builder(this)
            .setTitle("Registrar Combustible")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val bodyJson = JSONObject().apply {
                    put("vehicle_id",  etVehicleId.text.toString().toIntOrNull() ?: 0)
                    put("gallons",     etGallons.text.toString().toDoubleOrNull() ?: 0.0)
                    put("total_cost",  etTotalCost.text.toString().toDoubleOrNull() ?: 0.0)
                    put("date_filled", etDateFilled.text.toString())
                }.toString()

                HttpTask("POST", ApiConstants.FUEL, bodyJson, sessionManager.getToken()) { r ->
                    Toast.makeText(this,
                        if (r != null) "Combustible registrado ✔" else "Error",
                        Toast.LENGTH_SHORT).show()
                    loadFuel()
                }.execute()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
