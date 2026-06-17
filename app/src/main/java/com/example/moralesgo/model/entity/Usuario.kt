package com.example.moralesgo.model.entity

data class Usuario(
    val id_usuario: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val usuario: String,
    val rolId: Int // Rol 2 para Vendedor, por ejemplo
)