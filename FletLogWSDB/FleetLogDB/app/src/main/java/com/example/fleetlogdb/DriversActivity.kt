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
import com.example.fleetlogdb.model.Driver
import com.example.fleetlogdb.network.ApiConstants
import com.example.fleetlogdb.network.HttpTask
import com.example.fleetlogdb.ui.adapter.DriverAdapter
import com.example.fleetlogdb.utils.SessionManager
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pantalla de Gestión de Conductores.
 * Mismo patrón que MainActivity: AsyncTask + ListView + ArrayAdapter + AlertDialog.
 *
 * ENDPOINTS:
 *  GET    /api/drivers       → listar
 *  POST   /api/drivers       → crear  { name, license_number, phone }
 *  DELETE /api/drivers/:id   → eliminar
 *
 * Nota: El backend de drivers no tiene endpoint PUT (editar),
 * así que se implementa Crear y Eliminar (CRUD parcial según el backend).
 */
class DriversActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var driversList: MutableList<Driver>
    private lateinit var adapter: DriverAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)

        supportActionBar?.title = "Conductores"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(this)
        listView = findViewById(R.id.listViewDrivers)
        driversList = mutableListOf()
        adapter = DriverAdapter(this, driversList)
        listView.adapter = adapter

        // REQUISITO: Menú contextual en el ListView
        registerForContextMenu(listView)

        loadDrivers()
    }

    // --- GET /api/drivers ---
    private fun loadDrivers() {
        HttpTask("GET", ApiConstants.DRIVERS, null, sessionManager.getToken()) { result ->
            driversList.clear()
            if (result == null) {
                Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
                return@HttpTask
            }
            try {
                val arr = JSONArray(result)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    driversList.add(
                        Driver(
                            id            = obj.getInt("id"),
                            name          = obj.getString("name"),
                            licenseNumber = obj.getString("license_number"),
                            phone         = obj.optString("phone", "")
                        )
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al parsear conductores", Toast.LENGTH_SHORT).show()
            }
            adapter.notifyDataSetChanged()
        }.execute()
    }

    // --- MENÚ DE OPCIONES ---
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "Agregar Conductor")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            1 -> { showDriverFormDialog(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --- MENÚ CONTEXTUAL ---
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("Opciones del Conductor")
        menu?.add(0, 2, 0, "Eliminar")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val driver = driversList[info.position]
        if (item.itemId == 2) {
            confirmDelete(driver)
            return true
        }
        return super.onContextItemSelected(item)
    }

    // --- FORMULARIO: AlertDialog.Builder ---
    private fun showDriverFormDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_driver_form, null)
        val etName    = dialogView.findViewById<EditText>(R.id.etDriverName)
        val etLicense = dialogView.findViewById<EditText>(R.id.etLicenseNumber)
        val etPhone   = dialogView.findViewById<EditText>(R.id.etDriverPhone)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Conductor")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val bodyJson = JSONObject().apply {
                    put("name",           etName.text.toString())
                    put("license_number", etLicense.text.toString())
                    put("phone",          etPhone.text.toString())
                }.toString()

                HttpTask("POST", ApiConstants.DRIVERS, bodyJson, sessionManager.getToken()) { r ->
                    Toast.makeText(this,
                        if (r != null) "Conductor guardado ✔" else "Error al guardar",
                        Toast.LENGTH_SHORT).show()
                    loadDrivers()
                }.execute()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- CONFIRMAR BORRADO: AlertDialog.Builder ---
    private fun confirmDelete(driver: Driver) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Conductor")
            .setMessage("¿Eliminar a '${driver.name}'?")
            .setPositiveButton("Sí") { _, _ ->
                HttpTask("DELETE", ApiConstants.driverById(driver.id), null, sessionManager.getToken()) { r ->
                    Toast.makeText(this,
                        if (r != null) "Eliminado" else "Error",
                        Toast.LENGTH_SHORT).show()
                    loadDrivers()
                }.execute()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
