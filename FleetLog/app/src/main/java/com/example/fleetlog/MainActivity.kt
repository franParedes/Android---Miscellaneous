package com.example.fleetlog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fleetlog.data.VehicleRepository
import com.example.fleetlog.model.Vehicle
import com.example.fleetlog.ui.adapter.VehicleAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VehicleAdapter

    // Variable para recordar qué vehículo tocamos al abrir el menú contextual
    private var selectedVehicle: Vehicle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewVehicles)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = VehicleAdapter(
            vehiclesList = VehicleRepository.getVehicles(),
            onItemClicked = { vehicle ->
                // REQUISITO: Permitir ver un registro / editar
                showVehicleFormDialog(vehicle)
            },
            onItemLongClicked = { view, vehicle ->
                // REQUISITO: Menú contextual
                selectedVehicle = vehicle
                registerForContextMenu(view)
                openContextMenu(view)
                unregisterForContextMenu(view)
            }
        )
        recyclerView.adapter = adapter
    }

    // --- REQUISITO: MENÚ DE OPCIONES (Barra superior) ---
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                // null porque es un registro nuevo
                showVehicleFormDialog(null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --- REQUISITO: MENÚ CONTEXTUAL (Toque largo sobre un vehículo) ---
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("Opciones del Vehículo")
        // Agregamos las opciones (groupId, itemId, order, title)
        menu?.add(0, 1, 0, "Editar")
        menu?.add(0, 2, 0, "Eliminar")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val vehicle = selectedVehicle ?: return super.onContextItemSelected(item)

        // Usamos switch/case (when) para garantizar la mantenibilidad
        // si se agregan nuevos tipos de acciones desde el frontend.
        return when (item.itemId) {
            1 -> {
                showVehicleFormDialog(vehicle)
                true
            }
            2 -> {
                confirmDelete(vehicle)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    // --- REQUISITO: USO DE DIÁLOGOS (Agregar / Editar) ---
    private fun showVehicleFormDialog(vehicleToEdit: Vehicle?) {
        // Inflamos el diseño del formulario que creamos
        val dialogView = layoutInflater.inflate(R.layout.dialog_vehicle_form, null)

        val etBrand = dialogView.findViewById<EditText>(R.id.etBrand)
        val etModel = dialogView.findViewById<EditText>(R.id.etModel)
        val etPlate = dialogView.findViewById<EditText>(R.id.etPlate)
        val etYear = dialogView.findViewById<EditText>(R.id.etYear)
        val cbIsPickup = dialogView.findViewById<CheckBox>(R.id.cbIsPickup)

        // Si es edición, llenamos los campos con los datos existentes
        if (vehicleToEdit != null) {
            etBrand.setText(vehicleToEdit.brand)
            etModel.setText(vehicleToEdit.model)
            etPlate.setText(vehicleToEdit.plate)
            etYear.setText(vehicleToEdit.year.toString())
            cbIsPickup.isChecked = vehicleToEdit.isPickup
        }

        val isEditing = vehicleToEdit != null
        val title = if (isEditing) "Editar Vehículo" else "Nuevo Vehículo"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                // Extraer textos
                val brand = etBrand.text.toString()
                val model = etModel.text.toString()
                val plate = etPlate.text.toString()
                val yearStr = etYear.text.toString()
                val isPickup = cbIsPickup.isChecked

                // Validación simple
                if (brand.isEmpty() || model.isEmpty() || plate.isEmpty() || yearStr.isEmpty()) {
                    Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val year = yearStr.toIntOrNull() ?: 0

                if (isEditing) {
                    // Actualizar el objeto existente
                    vehicleToEdit!!.brand = brand
                    vehicleToEdit.model = model
                    vehicleToEdit.plate = plate
                    vehicleToEdit.year = year
                    vehicleToEdit.isPickup = isPickup
                    VehicleRepository.updateVehicle(vehicleToEdit)
                } else {
                    // Generar un nuevo ID y guardar
                    val newId = (VehicleRepository.getVehicles().maxOfOrNull { it.id } ?: 0) + 1
                    val newVehicle = Vehicle(newId, brand, model, plate, year, isPickup)
                    VehicleRepository.addVehicle(newVehicle)
                }

                // Refrescar la lista visual
                adapter.updateData(VehicleRepository.getVehicles())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Diálogo de confirmación para eliminar
    private fun confirmDelete(vehicle: Vehicle) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Vehículo")
            .setMessage("¿Estás seguro de eliminar el registro de ${vehicle.brand} ${vehicle.model}?")
            .setPositiveButton("Eliminar") { _, _ ->
                VehicleRepository.deleteVehicle(vehicle.id)
                adapter.updateData(VehicleRepository.getVehicles())
                Toast.makeText(this, "Vehículo eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}