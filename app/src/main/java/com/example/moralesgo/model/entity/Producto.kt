package com.example.moralesgo.model.entity

data class Producto(
    val id_producto: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val estado: Boolean,
    val categoria: Categoria,
    val color: Color?,
    val material: Material?,
    val imagenes: List<ProductoImagen>?,
    val tallas: List<ProductoTalla>?
)
