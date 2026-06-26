package com.example.moralesgo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moralesgo.R
import com.example.moralesgo.databinding.ItemProductoBinding
import com.example.moralesgo.model.entity.Producto

class ProductoAdapter(
    private var listaProductos: MutableList<Producto>
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {


    inner class ProductoViewHolder(
        val binding: ItemProductoBinding
    ) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]


        holder.binding.txtNombre.text = producto.nombre
        holder.binding.txtCategoria.text = producto.categoria.nombre
        holder.binding.txtPrecio.text = "S/ ${String.format("%.2f", producto.precio)}"


        val stockTotal = producto.tallas?.sumOf { it.stock } ?: 0
        holder.binding.txtStock.text = "● Stock: $stockTotal"


        val primeraUrlFoto = producto.imagenes?.firstOrNull()?.imagenUrl

        Glide.with(holder.itemView.context)
            .load(primeraUrlFoto)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(holder.binding.imgProducto)


        val abrirDetalleLogica = {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, com.example.moralesgo.ui.activity.DetalleProductoActivity::class.java)


            val productoJson = com.google.gson.Gson().toJson(producto)
            intent.putExtra("PRODUCTO_JSON", productoJson)

            context.startActivity(intent)
        }

        holder.binding.btnAnadir.setOnClickListener { abrirDetalleLogica() }
        holder.itemView.setOnClickListener { abrirDetalleLogica() }
    }

    override fun getItemCount(): Int = listaProductos.size


    fun actualizarLista(nuevaLista: List<Producto>) {
        listaProductos = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }
}