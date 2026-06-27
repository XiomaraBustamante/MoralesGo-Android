package com.example.moralesgo.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.example.moralesgo.R
import com.example.moralesgo.controller.network.VolleySingleton
import com.example.moralesgo.database.AppDatabase
import com.example.moralesgo.databinding.ActivityDetalleProductoBinding
import com.example.moralesgo.model.entity.CarritoLocal
import com.example.moralesgo.model.entity.Producto
import com.example.moralesgo.model.entity.ProductoTalla
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalleProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleProductoBinding
    private var productoSeleccionado: Producto? = null
    private var tallasDisponibles: MutableList<ProductoTalla> = mutableListOf()

    private var cantidadActual = 1
    private var maximoStockPermitido = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetalleProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jsonProducto = intent.getStringExtra("PRODUCTO_JSON")
        if (jsonProducto != null) {
            productoSeleccionado = Gson().fromJson(jsonProducto, Producto::class.java)

            productoSeleccionado?.tallas?.filter { it.stock > 0 }?.let {
                tallasDisponibles.addAll(it)
            }

            mostrarDatosProducto()
        } else {
            AlertDialog.Builder(this)
                .setTitle("⚠️ Error de Lectura")
                .setMessage("No se pudieron cargar los parámetros del calzado seleccionado.")
                .setPositiveButton("Regresar") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }

        binding.btnAtrasDetalleCard.setOnClickListener { finish() }
        binding.btnAtrasDetalle.setOnClickListener { finish() }

        binding.swipeRefreshDetalle.setColorSchemeColors(Color.parseColor("#5B4FFF"))
        binding.swipeRefreshDetalle.setOnRefreshListener {
            ejecutarSincronizacionFreshStock()
        }

        binding.btnMenosCantidad.setOnClickListener {
            if (cantidadActual > 1) {
                cantidadActual--
                binding.edtCantidadValor.setText(cantidadActual.toString())
            }
        }

        binding.btnMasCantidad.setOnClickListener {
            if (cantidadActual < maximoStockPermitido) {
                cantidadActual++
                binding.edtCantidadValor.setText(cantidadActual.toString())
            } else {
                Snackbar.make(binding.root, "📦 Límite alcanzado: Solo quedan $maximoStockPermitido unidades de esta talla.", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#E53935"))
                    .setTextColor(Color.WHITE)
                    .setAction("VER STOCKS") {
                        binding.tvDetalleStock.requestFocus()
                    }
                    .setActionTextColor(Color.YELLOW)
                    .show()
            }
        }

        binding.edtCantidadValor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                if (texto.isEmpty()) return

                val entrada = texto.toIntOrNull() ?: 1
                if (entrada > maximoStockPermitido && maximoStockPermitido > 0) {
                    cantidadActual = maximoStockPermitido
                    binding.edtCantidadValor.setText(maximoStockPermitido.toString())
                    binding.edtCantidadValor.setSelection(binding.edtCantidadValor.text.length)

                    Snackbar.make(binding.root, "⚠️ Cantidad ajustada al inventario real disponible ($maximoStockPermitido pares).", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#FF9800"))
                        .setTextColor(Color.WHITE)
                        .show()

                } else if (entrada < 1) {
                    AlertDialog.Builder(this@DetalleProductoActivity)
                        .setTitle("🛑 Cantidad No Permitida")
                        .setMessage("La cantidad mínima para procesar una venta en Calzados Morales es de 1 par.")
                        .setPositiveButton("Corregir") { dialog, _ ->
                            cantidadActual = 1
                            binding.edtCantidadValor.setText("1")
                            binding.edtCantidadValor.setSelection(1)
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    val nuevaCantidadString = entrada.toString()
                    if (texto != nuevaCantidadString) {
                        binding.edtCantidadValor.setText(nuevaCantidadString)
                        binding.edtCantidadValor.setSelection(nuevaCantidadString.length)
                    }
                    cantidadActual = entrada
                }
            }
        })

        binding.btnAgregarCarrito.setOnClickListener {
            val txtCantidad = binding.edtCantidadValor.text.toString()
            val cantidadAValidar = txtCantidad.toIntOrNull() ?: 0

            if (cantidadAValidar < 1) {
                AlertDialog.Builder(this)
                    .setTitle("🛑 Operación Cancelada")
                    .setMessage("No se puede registrar una orden de venta con 0 unidades en el carrito.")
                    .setPositiveButton("Entendido") { dialog, _ ->
                        cantidadActual = 1
                        binding.edtCantidadValor.setText("1")
                        dialog.dismiss()
                    }
                    .show()
                return@setOnClickListener
            }

            val posicionSeleccionada = binding.spnDetalleTalla.selectedItemPosition
            if (posicionSeleccionada == AdapterView.INVALID_POSITION || tallasDisponibles.isEmpty()) {
                Snackbar.make(binding.root, "❌ Debe seleccionar una curva de talla válida.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show()
                return@setOnClickListener
            }

            val varianteElegida = tallasDisponibles[posicionSeleccionada]
            val producto = productoSeleccionado ?: return@setOnClickListener

            val idProd = producto.id_producto
            val idTal = varianteElegida.talla.id_talla
            val nombreCalzado = producto.nombre
            val nombreTalla = varianteElegida.talla.nombre
            val nombreColor = producto.color?.nombre ?: "Sin Color"
            val precioVenta = producto.precio

            val database = AppDatabase.getDatabase(this)
            val carritoDao = database.carritodao()

            lifecycleScope.launch(Dispatchers.IO) {
                val itemExistente = carritoDao.buscarVariante(idProd, idTal)
                val cantidadPreviaEnCarrito = itemExistente?.cantidad ?: 0
                val nuevaCantidadTotalAcumulada = cantidadPreviaEnCarrito + cantidadActual

                if (nuevaCantidadTotalAcumulada > maximoStockPermitido) {
                    withContext(Dispatchers.Main) {
                        AlertDialog.Builder(this@DetalleProductoActivity)
                            .setTitle("⚠️ Alerta de Stock")
                            .setMessage("Inconsistencia en el pedido:\n\nYa tienes $cantidadPreviaEnCarrito par(es) asignados en el carrito y el inventario máximo actual en almacén es de $maximoStockPermitido unidades.")
                            .setPositiveButton("Entendido", null)
                            .show()
                    }
                    return@launch
                }

                if (itemExistente != null) {
                    itemExistente.cantidad = nuevaCantidadTotalAcumulada
                    carritoDao.actualizarItem(itemExistente)
                } else {
                    val nuevoElemento = CarritoLocal(
                        idProducto = idProd,
                        idTalla = idTal,
                        nombre = nombreCalzado,
                        tallaNombre = nombreTalla,
                        colorNombre = nombreColor,
                        precio = precioVenta,
                        cantidad = cantidadActual
                    )
                    carritoDao.insertarItem(nuevoElemento)
                }

                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@DetalleProductoActivity)
                        .setTitle("🎉¡Pedido Confirmado!")
                        .setMessage("Se registraron exitosamente $cantidadActual par(es) de la Talla $nombreTalla en la orden temporal de venta.")
                        .setPositiveButton("Seguir vendiendo") { dialog, _ ->
                            dialog.dismiss()
                            cantidadActual = 1
                            binding.edtCantidadValor.setText("1")
                        }

                        .setNegativeButton("Ir a Caja / Carrito") { _, _ ->
                            val intent = Intent(this@DetalleProductoActivity, CarritoActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }

    private fun mostrarDatosProducto() {
        productoSeleccionado?.let { prod ->
            binding.tvDetalleNombre.text = prod.nombre
            binding.tvDetalleCategoria.text = prod.categoria.nombre
            binding.tvDetallePrecio.text = "S/ ${String.format("%.2f", prod.precio)}"

            if (!prod.descripcion.isNullOrEmpty()) {
                binding.tvDetalleDescripcion.text = prod.descripcion
            }

            val stockTotal = tallasDisponibles.sumOf { it.stock }
            binding.tvBadgeStock.text = "● Stock Total Almacén: $stockTotal"

            val listaDeFotos = prod.imagenes?.map { it.imagenUrl }?.filter { it.isNotEmpty() } ?: emptyList()

            if (listaDeFotos.isNotEmpty()) {
                cargarImagenGrande(listaDeFotos[0])

                if (listaDeFotos.size >= 1) {
                    binding.cardMiniatura1.visibility = View.VISIBLE
                    Glide.with(this).load(listaDeFotos[0]).into(binding.imgMiniatura1)
                    binding.cardMiniatura1.setOnClickListener {
                        cargarImagenGrande(listaDeFotos[0])
                        actualizarBordeDestacado(1)
                    }
                } else { binding.cardMiniatura1.visibility = View.GONE }

                if (listaDeFotos.size >= 2) {
                    binding.cardMiniatura2.visibility = View.VISIBLE
                    Glide.with(this).load(listaDeFotos[1]).into(binding.imgMiniatura2)
                    binding.cardMiniatura2.setOnClickListener {
                        cargarImagenGrande(listaDeFotos[1])
                        actualizarBordeDestacado(2)
                    }
                } else { binding.cardMiniatura2.visibility = View.GONE }

                if (listaDeFotos.size >= 3) {
                    binding.cardMiniatura3.visibility = View.VISIBLE
                    Glide.with(this).load(listaDeFotos[2]).into(binding.imgMiniatura3)
                    binding.cardMiniatura3.setOnClickListener {
                        cargarImagenGrande(listaDeFotos[2])
                        actualizarBordeDestacado(3)
                    }
                } else { binding.cardMiniatura3.visibility = View.GONE }

            } else {
                binding.lytMiniaturas.visibility = View.GONE
                cargarImagenGrande(null)
            }

            if (tallasDisponibles.isNotEmpty()) {
                val listaNombresTallas = tallasDisponibles.map { it.talla.nombre }
                val adapterTallas = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNombresTallas)
                binding.spnDetalleTalla.adapter = adapterTallas

                binding.spnDetalleTalla.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val objetoTalla = tallasDisponibles[position]
                        maximoStockPermitido = objetoTalla.stock
                        binding.tvDetalleStock.text = "Talla ${objetoTalla.talla.nombre}: $maximoStockPermitido pares en stock"

                        if (cantidadActual > maximoStockPermitido) {
                            cantidadActual = maximoStockPermitido
                        } else if (cantidadActual < 1) {
                            cantidadActual = 1
                        }
                        binding.edtCantidadValor.setText(cantidadActual.toString())
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            } else {
                binding.spnDetalleTalla.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Agotado"))
                binding.tvDetalleStock.text = "Sin existencias en el almacén central."
                binding.edtCantidadValor.setText("0")
                binding.edtCantidadValor.isEnabled = false
                binding.btnMasCantidad.isEnabled = false
                binding.btnMenosCantidad.isEnabled = false
                binding.btnAgregarCarrito.isEnabled = false
                binding.btnAgregarCarrito.text = "PRODUCTO AGOTADO"
            }
        }
    }

    private fun cargarImagenGrande(url: String?) {
        Glide.with(this)
            .load(url)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(binding.imgDetalleProducto)
    }

    private fun actualizarBordeDestacado(seleccionado: Int) {
        binding.cardMiniatura1.strokeWidth = if (seleccionado == 1) 4 else 0
        binding.cardMiniatura2.strokeWidth = if (seleccionado == 2) 4 else 0
        binding.cardMiniatura3.strokeWidth = if (seleccionado == 3) 4 else 0
    }

    private fun ejecutarSincronizacionFreshStock() {
        val idProd = productoSeleccionado?.id_producto ?: return
        val urlApiFresh = "https://calzadosmorales-backend-production.up.railway.app/api/productos/api-detalle/$idProd"

        Log.d("MORALESGO", "Sincronizando producto completo desde la API: $urlApiFresh")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, urlApiFresh, null,
            { response ->
                try {
                    val productoActualizado = Gson().fromJson(response.toString(), Producto::class.java)
                    productoSeleccionado = productoActualizado

                    tallasDisponibles.clear()
                    productoActualizado.tallas?.filter { it.stock > 0 }?.let {
                        tallasDisponibles.addAll(it)
                    }

                    mostrarDatosProducto()
                    binding.swipeRefreshDetalle.isRefreshing = false

                    Snackbar.make(binding.root, "Información y existencias actualizadas", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#5B4FFF"))
                        .setTextColor(Color.WHITE)
                        .show()

                } catch (e: Exception) {
                    binding.swipeRefreshDetalle.isRefreshing = false
                    Log.e("MORALESGO_ERROR", "Error en el parseo del refresh: ${e.message}")
                }
            },
            { error ->
                binding.swipeRefreshDetalle.isRefreshing = false

                AlertDialog.Builder(this)
                    .setTitle("📡Error de Sincronización")
                    .setMessage("No se pudo obtener la información actualizada de Calzados Morales.\n\nPor favor, verifique que el servidor central esté activo o compruebe la conexión a internet de su dispositivo.")
                    .setPositiveButton("Entendido", null)
                    .show()
                    .also { dialog ->
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#5B4FFF"))
                    }
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }
}