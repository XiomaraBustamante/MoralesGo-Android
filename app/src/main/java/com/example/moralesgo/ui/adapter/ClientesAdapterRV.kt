package com.example.moralesgo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moralesgo.databinding.FilaClienteBinding
import com.example.moralesgo.model.entity.Clientes

class ClientesAdapterRV(
    private var listaClientes: MutableList<Clientes>
) : RecyclerView.Adapter<ClientesAdapterRV.ViewHolder>() {

    inner class ViewHolder(val binding: FilaClienteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FilaClienteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listaClientes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cliente = listaClientes[position]
        holder.binding.tvnomcli.text = cliente.nomcli
        holder.binding.tvcredito.text = "Crédito: S/ ${String.format("%.2f", cliente.credito)}"
        holder.binding.tvdatos.text = "Código: ${cliente.id}\nFecha: ${cliente.fechareg}"
    }

    fun actualizarLista(nuevaLista: MutableList<Clientes>) {
        listaClientes = nuevaLista
        notifyDataSetChanged()
    }
}