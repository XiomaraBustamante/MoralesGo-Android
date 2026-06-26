package com.example.moralesgo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moralesgo.R
import com.example.moralesgo.model.entity.CarritoLocal

class CarritoAdapter(
    private var items: List<CarritoLocal>,
    private val onEliminarClick: (CarritoLocal) -> Unit,
    private val onEditarClick: (CarritoLocal) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    class CarritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvProductoNombre)
        val tvDetalles: TextView = view.findViewById(R.id.tvProductoDetalles)
        val tvCantidad: TextView = view.findViewById(R.id.tvProductoCantidad)
        val tvPrecio: TextView = view.findViewById(R.id.tvProductoPrecio)
        val tvSubtotal: TextView = view.findViewById(R.id.tvProductoSubtotal)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarItem)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]

        holder.tvNombre.text = item.nombre
        holder.tvDetalles.text = "Talla: ${item.tallaNombre} | Color: ${item.colorNombre}"
        holder.tvCantidad.text = "Cant: ${item.cantidad}"
        holder.tvPrecio.text = "Precio: S/ %.2f".format(item.precio)
        holder.tvSubtotal.text = "S/ %.2f".format(item.subtotal)


        holder.btnEliminar.setOnClickListener { onEliminarClick(item) }
        holder.btnEditar.setOnClickListener { onEditarClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun actualizarLista(nuevosItems: List<CarritoLocal>) {
        this.items = nuevosItems
        notifyDataSetChanged()
    }
}