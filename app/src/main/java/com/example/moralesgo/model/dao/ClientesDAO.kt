package com.example.moralesgo.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moralesgo.model.entity.Clientes

@Dao
interface ClientesDAO {

    @Insert
    suspend fun AgregarCliente(obj: Clientes)


    @Update
    suspend fun ActualizarCliente(obj: Clientes)

    @Delete
    suspend fun EliminarCliente(obj: Clientes)

    @Query("SELECT * FROM Clientes")
    suspend fun ListarClientes(): List<Clientes>


    @Query("SELECT * FROM Clientes WHERE idCliente = :id LIMIT 1")
    suspend fun ObtenerClientePorId(id: Int): Clientes?
}