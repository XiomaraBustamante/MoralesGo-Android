package com.example.moralesgo.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Clientes")
data class Clientes(
    @PrimaryKey(autoGenerate = true)
    val idCliente: Int = 0,
    val tipoCliente: Int,


    val direccion: String,
    val telefono: String,
    val email: String,
    val fechareg: String,
    val estado: Boolean = true,

    val dni: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val genero: Int? = null,

    val ruc: String? = null,
    val razonSocial: String? = null,
    val repreLegal: String? = null
)