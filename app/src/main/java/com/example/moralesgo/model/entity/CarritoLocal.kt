package com.example.moralesgo.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "carrito_local")
data class CarritoLocal(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,


    var idProducto: Int,
    var idTalla: Int,


    var nombre: String,
    var tallaNombre: String,
    var colorNombre: String,
    var precio: Double,
    var cantidad: Int,


    var subtotal: Double = 0.0
) : Serializable