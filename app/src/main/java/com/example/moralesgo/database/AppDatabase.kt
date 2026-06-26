package com.example.moralesgo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moralesgo.model.dao.ClientesDAO
import com.example.moralesgo.model.dao.CarritoDao // 🔥 Agregado con tu estructura de paquete
import com.example.moralesgo.model.entity.Clientes
import com.example.moralesgo.model.entity.CarritoLocal // 🔥 Agregado con tu estructura de paquete

@Database(
    entities = [Clientes::class, CarritoLocal::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientesdao(): ClientesDAO
    abstract fun carritodao(): CarritoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Empresa.db"
                )

                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}