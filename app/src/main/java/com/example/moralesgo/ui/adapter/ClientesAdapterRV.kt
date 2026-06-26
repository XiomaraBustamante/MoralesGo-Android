package com.example.moralesgo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moralesgo.databinding.FilaClienteBinding
import com.example.moralesgo.model.entity.Clientes

class ClientesAdapterRV(
    private var listaClientes: MutableList<Clientes>,
    private val onClienteClick: (Clientes) -> Unit,
    private val onEditarClick: (Clientes) -> Unit,
    private val onEliminarClick: (Clientes) -> Unit
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


        if (cliente.tipoCliente == 1) {
            holder.binding.tvnomcli.text = "${cliente.nombre} ${cliente.apellido}"
            holder.binding.tvcredito.text = "DNI: ${cliente.dni} | Tel: ${cliente.telefono}"
        } else {
            holder.binding.tvnomcli.text = cliente.razonSocial
            holder.binding.tvcredito.text = "RUC: ${cliente.ruc} | Tel: ${cliente.telefono}"
        }

        holder.binding.tvdatos.text = "Dirección: ${cliente.direccion}\nFecha: ${cliente.fechareg}"


        holder.binding.root.setOnClickListener {
            onClienteClick(cliente)
        }


        holder.binding.btnEditarCliente.setOnClickListener {
            onEditarClick(cliente)
        }


        holder.binding.btnEliminarCliente.setOnClickListener {
            onEliminarClick(cliente)
        }
    }

    fun actualizarLista(nuevaLista: MutableList<Clientes>) {
        listaClientes = nuevaLista
        notifyDataSetChanged()
    }
}