package com.example.moralesgo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moralesgo.R
import com.example.moralesgo.model.entity.Producto

class ProductoAdapter(private val listaProductos: List<Producto>) :
    RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    // Se encarga de "inflar" el diseño de la tarjeta item_producto.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(vista)
    }

    // Une los datos del objeto Producto con los elementos del XML
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]
        holder.txtNombre.text = producto.nombre
        holder.txtCategoria.text = producto.categoria
        holder.txtPrecio.text = "S/ ${String.format("%.2f", producto.precio)}"
        holder.txtStock.text = "Stock: ${producto.stock}"

        // Carga la foto desde la URL usando Glide (Tema 9 del sílabo)
        Glide.with(holder.itemView.context)
            .load(producto.imagenUrl)
            .placeholder(R.mipmap.ic_launcher) // Foto por defecto si demora en cargar
            .into(holder.imgProducto)
    }

    override fun getItemCount(): Int = listaProductos.size

    // Mapea los componentes del XML de la tarjeta
    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView = itemView.findViewById(R.id.imgProducto)
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtCategoria: TextView = itemView.findViewById(R.id.txtCategoria)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        val txtStock: TextView = itemView.findViewById(R.id.txtStock)
    }
}