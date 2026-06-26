package com.example.moralesgo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moralesgo.databinding.ItemVentaBinding
import com.example.moralesgo.model.entity.VentaHistorial

class VentaAdapter(
    private var listaVentas: List<VentaHistorial>
) : RecyclerView.Adapter<VentaAdapter.VentaViewHolder>() {


    inner class VentaViewHolder(val binding: ItemVentaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val binding = ItemVentaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VentaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = listaVentas[position]

        with(holder.binding) {
            tvVentaCliente.text = "Cliente: ${venta.clienteNombre}"
            tvVentaFecha.text = venta.fecha
            tvVentaId.text = "Boleta N° ${venta.numeroBoleta}"
            tvMontoVenta.text = "S/ %.2f".format(venta.montoTotal)
        }
    }

    override fun getItemCount(): Int = listaVentas.size

    fun actualizarLista(nuevaLista: List<VentaHistorial>) {
        this.listaVentas = nuevaLista
        notifyDataSetChanged()
    }
}