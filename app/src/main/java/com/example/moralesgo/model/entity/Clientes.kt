package com.example.moralesgo.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Clientes") // Replicando la anotación exacta del profesor
data class Clientes(
    @PrimaryKey
    val id: Int, // Código o DNI del cliente
    val nomcli: String, // Nombre o Razón Social
    val credito: Double, // Línea de crédito asignada por el vendedor
    val fechareg: String // Fecha de la visita en el campo
)