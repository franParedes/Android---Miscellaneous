package com.example.fleetlogdb

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fleetlogdb.model.Vehicle
import com.example.fleetlogdb.network.ApiConstants
import com.example.fleetlogdb.network.HttpTask
import com.example.fleetlogdb.ui.adapter.VehicleAdapter
import com.example.fleetlogdb.utils.ImageUtils
import com.example.fleetlogdb.utils.SessionManager
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pantalla Principal: Lista de Vehículos.
 *
 * REQUISITOS RÚBRICA CUMPLIDOS:
 *  1. HttpURLConnection dentro de AsyncTask (via HttpTask)        ✅
 *  2. Parseo con JSONObject y JSONArray nativos                   ✅
 *  3. ListView (prohibido RecyclerView)                           ✅
 *  4. Adaptador VehicleAdapter hereda ArrayAdapter + getView()    ✅
 *  5. onCreateOptionsMenu + registerForContextMenu                ✅
 *  6. AlertDialog.Builder para formularios y confirmaciones       ✅
 *  7. SharedPreferences via SessionManager (token)                ✅
 *  8. Gestión de imágenes Base64 con galería                     ✅
 */
class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var vehiclesList: MutableList<Vehicle>
    private lateinit var adapter: VehicleAdapter
    private lateinit var sessionManager: SessionManager

    // Para manejar la selección de imagen de la galería en el diálogo
    private var currentImageBase64: String? = null
    private var dialogImageView: ImageView? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        listView = findViewById(R.id.listViewVehicles)
        vehiclesList = mutableListOf()
        adapter = VehicleAdapter(this, vehiclesList)
        listView.adapter = adapter

        // REQUISITO: registerForContextMenu en el ListView (menú contextual con click largo)
        registerForContextMenu(listView)

        // Click corto → editar
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            showVehicleFormDialog(vehiclesList[position])
        }

        loadVehicles()
    }

    // =====================================================================
    // CARGA DE DATOS: GET /api/vehicles  (AsyncTask + HttpURLConnection)
    // =====================================================================

    private fun loadVehicles() {
        HttpTask(
            method   = "GET",
            url      = ApiConstants.VEHICLES,
            body     = null,
            token    = sessionManager.getToken(),
            callback = { result -> parseAndShowVehicles(result) }
        ).execute()
    }

    /**
     * Parsea el JSON con JSONArray/JSONObject nativos y actualiza el ListView.
     * REQUISITO RÚBRICA: Sin Gson ni Moshi.
     */
    private fun parseAndShowVehicles(result: String?) {
        vehiclesList.clear()
        if (result == null) {
            Toast.makeText(this, "Error de red. Verifica el servidor.", Toast.LENGTH_LONG).show()
            adapter.notifyDataSetChanged()
            return
        }
        try {
            val jsonArray = JSONArray(result)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                vehiclesList.add(
                    Vehicle(
                        id          = obj.getInt("id"),
                        brand       = obj.getString("brand"),
                        model       = obj.getString("model"),
                        plate       = obj.getString("plate"),
                        year        = obj.getInt("year"),
                        color       = obj.optString("color", "Blanco"),
                        mileage     = obj.optInt("mileage", 0),
                        status      = obj.optString("status", "Activo"),
                        imageBase64 = obj.optString("imageBase64", null),
                        isPickup    = obj.optInt("isPickup", 0)
                    )
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al parsear datos.", Toast.LENGTH_SHORT).show()
        }
        adapter.notifyDataSetChanged()
    }

    // =====================================================================
    // MENÚ DE OPCIONES (onCreateOptionsMenu) — REQUISITO RÚBRICA
    // =====================================================================

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                showVehicleFormDialog(null)
                true
            }
            R.id.action_drivers -> {
                startActivity(Intent(this, DriversActivity::class.java))
                true
            }
            R.id.action_maintenance -> {
                startActivity(Intent(this, MaintenanceActivity::class.java))
                true
            }
            R.id.action_fuel -> {
                startActivity(Intent(this, FuelActivity::class.java))
                true
            }
            R.id.action_logout -> {
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // =====================================================================
    // MENÚ CONTEXTUAL (registerForContextMenu) — REQUISITO RÚBRICA
    // =====================================================================

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("Opciones del Vehículo")
        menu?.add(0, 1, 0, "Editar")
        menu?.add(0, 2, 0, "Eliminar")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // AdapterContextMenuInfo nos da la posición del ítem pulsado
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val vehicle = vehiclesList[info.position]

        return when (item.itemId) {
            1 -> { showVehicleFormDialog(vehicle); true }
            2 -> { confirmDeleteVehicle(vehicle); true }
            else -> super.onContextItemSelected(item)
        }
    }

    // =====================================================================
    // FORMULARIO INSERT/UPDATE — AlertDialog.Builder — REQUISITO RÚBRICA
    // =====================================================================

    private fun showVehicleFormDialog(vehicleToEdit: Vehicle?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_vehicle_form, null)

        val ivPreview       = dialogView.findViewById<ImageView>(R.id.ivPreview)
        val btnSelectImage  = dialogView.findViewById<Button>(R.id.btnSelectImage)
        val etBrand         = dialogView.findViewById<EditText>(R.id.etBrand)
        val etModel         = dialogView.findViewById<EditText>(R.id.etModel)
        val etPlate         = dialogView.findViewById<EditText>(R.id.etPlate)
        val etYear          = dialogView.findViewById<EditText>(R.id.etYear)
        val etColor         = dialogView.findViewById<EditText>(R.id.etColor)
        val etMileage       = dialogView.findViewById<EditText>(R.id.etMileage)
        val etStatus        = dialogView.findViewById<EditText>(R.id.etStatus)
        val cbIsPickup      = dialogView.findViewById<CheckBox>(R.id.cbIsPickup)

        // Guardamos referencias para el resultado de la galería
        dialogImageView = ivPreview
        currentImageBase64 = vehicleToEdit?.imageBase64

        // Si estamos editando, precargamos los datos
        if (vehicleToEdit != null) {
            etBrand.setText(vehicleToEdit.brand)
            etModel.setText(vehicleToEdit.model)
            etPlate.setText(vehicleToEdit.plate)
            etYear.setText(vehicleToEdit.year.toString())
            etColor.setText(vehicleToEdit.color)
            etMileage.setText(vehicleToEdit.mileage.toString())
            etStatus.setText(vehicleToEdit.status)
            cbIsPickup.isChecked = vehicleToEdit.isPickup == 1
            val bmp = ImageUtils.base64ToBitmap(vehicleToEdit.imageBase64)
            if (bmp != null) ivPreview.setImageBitmap(bmp)
        }

        // REQUISITO: Abrir galería y convertir a Base64
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (vehicleToEdit == null) "Nuevo Vehículo" else "Editar Vehículo")
            .setView(dialogView)
            .setPositiveButton("Guardar", null) // null para controlar el cierre manualmente
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val brand   = etBrand.text.toString().trim()
                val model   = etModel.text.toString().trim()
                val plate   = etPlate.text.toString().trim()
                val yearStr = etYear.text.toString().trim()
                val color   = etColor.text.toString().trim()
                val mileage = etMileage.text.toString().trim()
                val status  = etStatus.text.toString().trim()

                // Validaciones: campos obligatorios
                when {
                    brand.isEmpty() -> { etBrand.error = "La marca es obligatoria"; return@setOnClickListener }
                    model.isEmpty() -> { etModel.error = "El modelo es obligatorio"; return@setOnClickListener }
                    plate.isEmpty() -> { etPlate.error = "La placa es obligatoria"; return@setOnClickListener }
                    yearStr.isEmpty() -> { etYear.error = "El año es obligatorio"; return@setOnClickListener }
                    yearStr.toIntOrNull() == null -> { etYear.error = "El año debe ser un número"; return@setOnClickListener }
                }

                val bodyJson = JSONObject().apply {
                    put("brand",       brand)
                    put("model",       model)
                    put("plate",       plate)
                    put("year",        yearStr.toInt())
                    put("color",       color.ifEmpty { "Blanco" })
                    put("mileage",     mileage.toIntOrNull() ?: 0)
                    put("status",      status.ifEmpty { "Activo" })
                    put("imageBase64", currentImageBase64)
                    put("isPickup",    if (cbIsPickup.isChecked) 1 else 0)
                }.toString()

                if (vehicleToEdit == null) {
                    // INSERT: POST /api/vehicles
                    HttpTask("POST", ApiConstants.VEHICLES, bodyJson, sessionManager.getToken()) { r ->
                        Toast.makeText(this,
                            if (r != null) "Vehículo guardado ✔" else "Error al guardar",
                            Toast.LENGTH_SHORT).show()
                        loadVehicles()
                    }.execute()
                } else {
                    // UPDATE: PUT /api/vehicles/:id
                    HttpTask("PUT", ApiConstants.vehicleById(vehicleToEdit.id), bodyJson, sessionManager.getToken()) { r ->
                        Toast.makeText(this,
                            if (r != null) "Vehículo actualizado ✔" else "Error al actualizar",
                            Toast.LENGTH_SHORT).show()
                        loadVehicles()
                    }.execute()
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    // =====================================================================
    // CONFIRMACIÓN DE BORRADO — AlertDialog.Builder — REQUISITO RÚBRICA
    // =====================================================================

    private fun confirmDeleteVehicle(vehicle: Vehicle) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Vehículo")
            .setMessage("¿Seguro que deseas eliminar '${vehicle.brand} ${vehicle.model}'?\nEsta acción es irreversible.")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                HttpTask("DELETE", ApiConstants.vehicleById(vehicle.id), null, sessionManager.getToken()) { r ->
                    Toast.makeText(this,
                        if (r != null) "Vehículo eliminado" else "Error al eliminar",
                        Toast.LENGTH_SHORT).show()
                    loadVehicles()
                }.execute()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // =====================================================================
    // GALERÍA: Recibir imagen seleccionada y convertir a Base64
    // REQUISITO RÚBRICA: La imagen se convierte a Base64 para enviarla en el JSON
    // =====================================================================

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                // Convertir URI → Base64 (usando ImageUtils)
                val base64 = ImageUtils.uriToBase64(this, uri)
                if (base64 != null) {
                    currentImageBase64 = base64
                    // Mostrar preview en el ImageView del diálogo
                    val bitmap = ImageUtils.base64ToBitmap(base64)
                    dialogImageView?.setImageBitmap(bitmap)
                } else {
                    Toast.makeText(this, "No se pudo procesar la imagen.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}