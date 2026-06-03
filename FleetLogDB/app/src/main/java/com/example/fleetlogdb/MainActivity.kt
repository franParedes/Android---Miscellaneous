package com.example.fleetlogdb

import android.content.ContentValues
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fleetlogdb.data.VehicleSQLHelper
import com.example.fleetlogdb.model.Vehicle
import com.example.fleetlogdb.ui.adapter.VehicleAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var vehiclesList: MutableList<Vehicle>
    private lateinit var dbHelper: VehicleSQLHelper
    private lateinit var adapter: VehicleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ////////****** INICIALIZACION DE LA BASE DE DATOS ******////////
        dbHelper = VehicleSQLHelper(this)
        listView = findViewById(R.id.listViewVehicles)
        vehiclesList = mutableListOf()

        // Registramos el ListView para que tenga Menú Contextual (Click largo)
        registerForContextMenu(listView)

        // Click corto para Editar/Ver un registro
        /*
        * Asignamos la propiedad usando el signo '=' y la interfaz correspondiente Conversion SAM - Single Abstract Method
        * "Quiero que esta propiedad use la interfaz OnItemClickListener de la clase AdapterView"
        */
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val vehicle = vehiclesList[position]
            showVehicleFormDialog(vehicle)
        }

        loadData()
    }

    private fun loadData() {
        vehiclesList.clear()
        val db = dbHelper.readableDatabase

        // Ejecutamos la consulta. El resultado se maneja con un Cursor
        val cursor = db.query("VEHICLES", null, null, null, null, null, null)

        // Recorremos las filas resultantes
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
            val brand = cursor.getString(cursor.getColumnIndexOrThrow("BRAND"))
            val model = cursor.getString(cursor.getColumnIndexOrThrow("MODEL"))
            val plate = cursor.getString(cursor.getColumnIndexOrThrow("PLATE"))
            val year = cursor.getInt(cursor.getColumnIndexOrThrow("YEAR"))
            val isPickup = cursor.getInt(cursor.getColumnIndexOrThrow("ISPICKUP"))

            vehiclesList.add(Vehicle(id, brand, model, plate, year, isPickup))
        }
        cursor.close() // Cerramos el cursor

        // Refrescamos nuestro adaptador clásico
        adapter = VehicleAdapter(this, vehiclesList)
        listView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add) {
            showVehicleFormDialog(null) // null indica que es un registro nuevo
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    // --- MENÚ CONTEXTUAL CLÁSICO (Estilo de la clase) ---
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("Opciones")
        menu?.add(0, 1, 0, "Editar")
        menu?.add(0, 2, 0, "Eliminar")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Aquí obtenemos qué fila se tocó
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val vehicle = vehiclesList[info.position]

        return when (item.itemId) {
            1 -> {
                showVehicleFormDialog(vehicle)
                true
            }
            2 -> {
                deleteVehicle(vehicle.id)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    // --- DIÁLOGOS Y ESCRITURA EN BASE DE DATOS (Insert/Update) ---
    private fun showVehicleFormDialog(vehicleToEdit: Vehicle?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_vehicle_form, null)

        val etBrand = dialogView.findViewById<EditText>(R.id.etBrand)
        val etModel = dialogView.findViewById<EditText>(R.id.etModel)
        val etPlate = dialogView.findViewById<EditText>(R.id.etPlate)
        val etYear = dialogView.findViewById<EditText>(R.id.etYear)
        val cbIsPickup = dialogView.findViewById<CheckBox>(R.id.cbIsPickup)

        // Si estamos editando, rellenamos los campos
        if (vehicleToEdit != null) {
            etBrand.setText(vehicleToEdit.brand)
            etModel.setText(vehicleToEdit.model)
            etPlate.setText(vehicleToEdit.plate)
            etYear.setText(vehicleToEdit.year.toString())
            cbIsPickup.isChecked = vehicleToEdit.isPickup == 1
        }

        AlertDialog.Builder(this)
            .setTitle(if (vehicleToEdit == null) "Nuevo Vehículo" else "Editar Vehículo")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->

                // Usamos ContentValues para mapear las columnas con los textos
                val values = ContentValues().apply {
                    put("BRAND", etBrand.text.toString())
                    put("MODEL", etModel.text.toString())
                    put("PLATE", etPlate.text.toString())
                    put("YEAR", etYear.text.toString().toIntOrNull() ?: 0)
                    put("ISPICKUP", if (cbIsPickup.isChecked) 1 else 0)
                }

                val db = dbHelper.writableDatabase

                if (vehicleToEdit == null) {
                    // INSERTAR
                    db.insert("VEHICLES", null, values)
                    Toast.makeText(this, "Guardado en BD", Toast.LENGTH_SHORT).show()
                } else {
                    // ACTUALIZAR
                    val whereClause = "_id = ?"
                    val whereArgs = arrayOf(vehicleToEdit.id.toString())
                    db.update("VEHICLES", values, whereClause, whereArgs)
                    Toast.makeText(this, "Actualizado en BD", Toast.LENGTH_SHORT).show()
                }

                loadData() // Recargamos la lista desde la Base de Datos
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- BORRAR DE LA BASE DE DATOS ---
    private fun deleteVehicle(id: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Seguro que deseas eliminar este registro permanentemente?")
            .setPositiveButton("Sí") { _, _ ->
                val db = dbHelper.writableDatabase

                // Sentencia DELETE
                db.delete("VEHICLES", "_id = ?", arrayOf(id.toString()))

                Toast.makeText(this, "Eliminado de la BD", Toast.LENGTH_SHORT).show()
                loadData() // Recargamos la lista
            }
            .setNegativeButton("No", null)
            .show()
    }
}