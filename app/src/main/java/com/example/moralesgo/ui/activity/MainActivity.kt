package com.example.moralesgo.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.example.moralesgo.controller.network.VolleySingleton
import com.example.moralesgo.databinding.ActivityMainBinding
import com.example.moralesgo.model.entity.Producto
import com.example.moralesgo.ui.adapter.ProductoAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var productoAdapter: ProductoAdapter

    private var listaCompletaProductos = mutableListOf<Producto>()
    private var listaFiltradaBusqueda = mutableListOf<Producto>()

    // Referencia al Chip de "Todos" dinámico
    private var chipTodosDinamico: Chip? = null

    // Rutas base de conexión a la API
    private val URL_API_PRODUCTOS = "http://10.0.2.2:8068/api/productos/listar"
    private val URL_API_CATEGORIAS = "http://10.0.2.2:8068/api/categorias/listar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvProductos.layoutManager = GridLayoutManager(this, 2)

        productoAdapter = ProductoAdapter(mutableListOf())
        binding.rvProductos.adapter = productoAdapter

        binding.swipeRefresh.setColorSchemeColors(Color.parseColor("#5B4FFF"))

        binding.swipeRefresh.post {
            binding.swipeRefresh.isRefreshing = true
        }

        binding.swipeRefresh.setOnRefreshListener {
            Log.d("MORALESGO", "Refrescando catálogo y categorías desde la nube...")
            binding.etBuscar.setText("")
            cargarDatosDesdeApi()
        }

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarCalzadosPorNombre(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        binding.btnCerrarSesion.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas salir por completo del sistema MoralesGo?")
                .setPositiveButton("Salir") { _, _ ->

                    finishAffinity()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }


        binding.btnVerHistorialVentasCard.setOnClickListener {
            Log.d("MORALESGO", "Abriendo el historial de ventas centralizado...")
            val intent = Intent(this, HistorialVentasActivity::class.java)
            startActivity(intent)
        }


        binding.imgAvatar.setOnClickListener {
            val intent = Intent(this, ListarClientesActivity::class.java)
            startActivity(intent)
        }


        binding.btnVerCarrito.setOnClickListener {
            Log.d("MORALESGO", "Abriendo el carrito de compras...")
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        cargarDatosDesdeApi()
    }


    private fun cargarDatosDesdeApi() {
        cargarCategoriasDinamicas()
        cargarProductosDesdeApi()
    }


    private fun cargarCategoriasDinamicas() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, URL_API_CATEGORIAS, null,
            { response ->
                try {

                    binding.containerChips.removeAllViews()


                    chipTodosDinamico = Chip(this).apply {
                        text = "Todos"
                        id = ViewGroup.generateViewId()
                        setOnClickListener {
                            binding.etBuscar.setText("")
                            marcarChipActivo(this)
                            productoAdapter.actualizarLista(listaCompletaProductos)
                        }
                    }
                    binding.containerChips.addView(chipTodosDinamico)
                    chipTodosDinamico?.let { marcarChipActivo(it) }


                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val nombreCat = obj.getString("nombre")

                        val nuevoChip = Chip(this).apply {
                            text = nombreCat
                            id = ViewGroup.generateViewId()
                            setOnClickListener {
                                binding.etBuscar.setText("")
                                marcarChipActivo(this)
                                filtrarCatalogoPorNombreCategoria(nombreCat)
                            }
                        }


                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(16, 0, 0, 0)
                        }
                        nuevoChip.layoutParams = params

                        binding.containerChips.addView(nuevoChip)
                    }

                } catch (e: Exception) {
                    Log.e("MORALESGO", "Error procesando categorías dinámicas: ${e.message}")
                }
            },
            { error ->
                Log.e("MORALESGO", "Error de red al traer categorías: ${error.message}")
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest)
    }

    private fun cargarProductosDesdeApi() {
        Log.d("MORALESGO", "Iniciando petición HTTP GET a: $URL_API_PRODUCTOS")

        val jsonArrayRequest = object : JsonArrayRequest(
            Request.Method.GET, URL_API_PRODUCTOS, null,
            { response ->
                binding.swipeRefresh.isRefreshing = false
                Log.d("MORALESGO", "Respuesta recibida: ${response.length()} items")
                try {
                    val gson = Gson()
                    val tipoLista = object : TypeToken<List<Producto>>() {}.type
                    val productosDescargados: List<Producto> = gson.fromJson(response.toString(), tipoLista)

                    listaCompletaProductos = productosDescargados.toMutableList()

                    listaFiltradaBusqueda.clear()
                    listaFiltradaBusqueda.addAll(listaCompletaProductos)

                    productoAdapter.actualizarLista(listaFiltradaBusqueda)
                    chipTodosDinamico?.let { marcarChipActivo(it) }

                    Snackbar.make(
                        binding.root,
                        "📦Sincronizado: Catálogo actualizado en tiempo real",
                        Snackbar.LENGTH_SHORT
                    ).setBackgroundTint(Color.parseColor("#10B981"))
                        .setTextColor(Color.WHITE)
                        .show()

                } catch (e: Exception) {
                    Log.e("MORALESGO", "Error GSON al parsear: ${e.message}")
                    Snackbar.make(binding.root, "Error al procesar datos del catálogo", Snackbar.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.swipeRefresh.isRefreshing = false
                Log.e("MORALESGO", "Error de Red Volley: ${error.message}")
                Snackbar.make(binding.root, "No se pudo conectar con el servidor cloud", Snackbar.LENGTH_LONG).show()
            }
        ) {
            override fun getRetryPolicy() = com.android.volley.DefaultRetryPolicy(
                15000, 1, 1.0f
            )
        }
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest)
    }

    private fun filtrarCalzadosPorNombre(texto: String) {
        listaFiltradaBusqueda.clear()
        if (texto.isEmpty()) {
            listaFiltradaBusqueda.addAll(listaCompletaProductos)
        } else {
            val query = texto.lowercase().trim()
            for (item in listaCompletaProductos) {
                if (item.nombre.lowercase().contains(query)) {
                    listaFiltradaBusqueda.add(item)
                }
            }
        }

        productoAdapter.actualizarLista(listaFiltradaBusqueda)


        if (listaFiltradaBusqueda.isEmpty()) {
            binding.swipeRefresh.visibility = View.GONE
            binding.lytEmptyState.visibility = View.VISIBLE
            binding.tvEmptyMessage.text = "No se encontraron resultados para tu búsqueda por \"$texto\""
        } else {
            binding.swipeRefresh.visibility = View.VISIBLE
            binding.lytEmptyState.visibility = View.GONE
        }
    }

    private fun filtrarCatalogoPorNombreCategoria(nombreCategoria: String) {
        val listaFiltrada = listaCompletaProductos.filter {
            it.categoria.nombre.equals(nombreCategoria, ignoreCase = true)
        }
        productoAdapter.actualizarLista(listaFiltrada)


        if (listaFiltrada.isEmpty()) {
            binding.swipeRefresh.visibility = View.GONE
            binding.lytEmptyState.visibility = View.VISIBLE
            binding.tvEmptyMessage.text = "Actualmente no contamos con modelos disponibles en la categoría \"$nombreCategoria\""
        } else {
            binding.swipeRefresh.visibility = View.VISIBLE
            binding.lytEmptyState.visibility = View.GONE
        }
    }

    private fun marcarChipActivo(chipSeleccionado: Chip) {
        val count = binding.containerChips.childCount
        for (i in 0 until count) {
            val child = binding.containerChips.getChildAt(i)
            if (child is Chip) {
                if (child == chipSeleccionado) {
                    child.setChipBackgroundColorResource(android.R.color.transparent)
                    child.chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#5B4FFF"))
                    child.setTextColor(Color.WHITE)
                    child.chipStrokeWidth = 0f
                } else {
                    child.setChipBackgroundColorResource(android.R.color.transparent)
                    child.chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.WHITE)
                    child.setTextColor(Color.parseColor("#5B4FFF"))
                    child.chipStrokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#C4B5E8"))
                    child.chipStrokeWidth = 1f
                }
            }
        }
    }
}