package com.example.moralesgo.model.dao
import com.example.moralesgo.model.entity.Producto

interface ProductoDao {
    fun listarProductos(callback: (List<Producto>?, String?) -> Unit)
    // El callback devolverá la lista de zapatillas de Railway o un mensaje de error si falla

}