package com.example.moralesgo.model.dao

import androidx.room.*
import com.example.moralesgo.model.entity.CarritoLocal

@Dao
public interface CarritoDao {

    @Query("SELECT * FROM carrito_local")
    suspend fun obtenerCarrito(): List<CarritoLocal>

    @Query("SELECT * FROM carrito_local WHERE idProducto = :idProd AND idTalla = :idTal LIMIT 1")
    suspend fun buscarVariante(idProd: Int, idTal: Int): CarritoLocal?

    @Insert
    suspend fun insertarItem(item: CarritoLocal)

    @Update
    suspend fun actualizarItem(item: CarritoLocal)

    @Delete
    suspend fun eliminarItem(item: CarritoLocal)

    @Query("DELETE FROM carrito_local")
    suspend fun vaciarCarrito()
}