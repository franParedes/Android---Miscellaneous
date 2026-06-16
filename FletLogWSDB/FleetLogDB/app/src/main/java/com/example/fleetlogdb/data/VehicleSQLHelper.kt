package com.example.fleetlogdb.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class VehicleSQLHelper(context: Context) : SQLiteOpenHelper(context, "FleetLogDB", null, 1) {
    // Comando SQL para crear la tabla
    private val SQL_CREATE_ENTRIES = """
        CREATE TABLE VEHICLES (
            _id INTEGER PRIMARY KEY AUTOINCREMENT,
            BRAND TEXT,
            MODEL TEXT,
            PLATE TEXT,
            YEAR INTEGER,
            ISPICKUP INTEGER
        )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS VEHICLES")
        onCreate(db)
    }
}