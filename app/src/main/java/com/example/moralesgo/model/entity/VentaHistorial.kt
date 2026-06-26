package com.example.moralesgo.model.entity

import com.google.gson.annotations.SerializedName

data class VentaHistorial(
    @SerializedName("id_venta")
    val idVenta: Int,

    @SerializedName("cliente_nombre")
    val clienteNombre: String,

    @SerializedName("monto_total")
    val montoTotal: Double,

    @SerializedName("fecha_registro")
    val fecha: String,

    @SerializedName("numero_boleta")
    val numeroBoleta: String
)