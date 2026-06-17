package com.example.moralesgo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moralesgo.model.dao.ClientesDAO

import com.example.moralesgo.model.entity.Clientes

@Database(
    entities = [Clientes::class], // Registramos la entidad de clientes de campo
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientesdao(): ClientesDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null // Patrón de hilo seguro [cite: 15, 16]

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // Bloqueo de concurrencia [cite: 16]
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Empresa.db" // Nombre del archivo SQLite local [cite: 17, 18]
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}