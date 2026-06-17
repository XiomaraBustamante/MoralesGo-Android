package com.example.moralesgo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moralesgo.R
import com.example.moralesgo.databinding.ItemProductoBinding
import com.example.moralesgo.model.entity.Producto

class ProductoAdapter(
    private var listaProductos: MutableList<Producto> // Cambiado a MutableList para permitir actualizaciones con SwipeRefresh
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    // Utiliza el patrón inner class con ViewBinding siguiendo la estructura exacta del profesor
    inner class ProductoViewHolder(
        val binding: ItemProductoBinding
    ) : RecyclerView.ViewHolder(binding.root)

    // Se encarga de inflar el diseño usando ViewBinding (Adiós a LayoutInflater.inflate clásico)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    // Une los datos del objeto Producto real con los componentes visuales mediante Binding
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        // Asignación de textos limpios usando el objeto binding
        holder.binding.txtNombre.text = producto.nombre
        holder.binding.txtCategoria.text = producto.categoria.nombre
        holder.binding.txtPrecio.text = "S/ ${String.format("%.2f", producto.precio)}"

        // 👟 CÁLCULO DE STOCK DINÁMICO: Sumamos los stocks individuales de toda la curva de tallas
        val stockTotal = producto.tallas?.sumOf { it.stock } ?: 0
        holder.binding.txtStock.text = "● Stock: $stockTotal"

        // 🔥 CONTROL DE IMÁGENES CLOUDINARY: Tomamos la primera foto del carrusel si existe
        val primeraUrlFoto = producto.imagenes?.firstOrNull()?.imagenUrl

        Glide.with(holder.itemView.context)
            .load(primeraUrlFoto) // Descarga la URL de internet en tiempo real
            .placeholder(R.mipmap.ic_launcher) // Foto por defecto mientras descarga
            .error(R.mipmap.ic_launcher) // Foto por defecto si falla el internet o el enlace está roto
            .into(holder.binding.imgProducto)

        // Evento del botón para el flujo del carrito de ventas del vendedor
        holder.binding.btnAnadir.setOnClickListener {
            // Aquí irá la lógica de añadir al SQLite local mediante Room cuando creemos la venta
        }
    }

    override fun getItemCount(): Int = listaProductos.size

    // Función de utilidad premium para refrescar el catálogo completo al usar el SwipeRefreshLayout
    fun actualizarLista(nuevaLista: List<Producto>) {
        listaProductos = nuevaLista.toMutableList()
        notifyDataSetChanged() // Notifica al RecyclerView que redibuje las tarjetas con los datos nuevos de Railway
    }
}