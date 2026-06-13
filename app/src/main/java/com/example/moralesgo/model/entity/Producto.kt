package com.example.moralesgo.model.entity

data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String? = null
)