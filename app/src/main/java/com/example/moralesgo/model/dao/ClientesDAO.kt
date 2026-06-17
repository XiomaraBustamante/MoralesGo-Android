package com.example.moralesgo.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moralesgo.model.entity.Clientes

@Dao // Protocolo de persistencia nativa de Room
interface ClientesDAO {

    @Insert
    suspend fun AgregarCliente(obj: Clientes) // Las corrutinas evitan bloquear la pantalla

    @Update
    suspend fun ActualizarCliente(obj: Clientes)

    @Delete
    suspend fun EliminarCliente(obj: Clientes)

    @Query("SELECT * FROM Clientes")
    suspend fun ListarClientes(): List<Clientes>

    @Query("SELECT * FROM Clientes WHERE id = :id LIMIT 1")
    suspend fun ClientePorId(id: Int): Clientes // Indispensable para la pantalla de edición [cite: 97]
}