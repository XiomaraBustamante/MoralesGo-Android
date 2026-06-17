package com.example.moralesgo.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.example.moralesgo.controller.network.VolleySingleton
import com.example.moralesgo.databinding.ActivityMainBinding
import com.example.moralesgo.model.entity.Producto
import com.example.moralesgo.ui.adapter.ProductoAdapter
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    // 🚀 Implementación de ViewBinding siguiendo la metodología del profesor
    private lateinit var binding: ActivityMainBinding
    private lateinit var productoAdapter: ProductoAdapter

    // Listas en memoria para manejar los filtros del catálogo de forma veloz
    private var listaCompletaProductos = mutableListOf<Producto>()

    // Ruta base de conexión (Modificar por tu dominio de Railway al pasar a producción)
    private val URL_API = "http://10.0.2.2:8068/api/productos/listar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización del ViewBinding [cite: 5, 48]
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Configurar el RecyclerView en cuadrícula de 2 columnas usando Binding
        binding.rvProductos.layoutManager = GridLayoutManager(this, 2)

        // Inicializamos el adaptador vacío por primera vez para evitar crash de inicialización [cite: 51]
        productoAdapter = ProductoAdapter(mutableListOf())
        binding.rvProductos.adapter = productoAdapter // [cite: 53]

        // 2. Configurar el SwipeRefreshLayout con el color morado institucional
        binding.swipeRefresh.setColorSchemeColors(Color.parseColor("#5B4FFF"))

        // Escuchador del gesto de deslizar hacia abajo para refrescar desde Railway
        binding.swipeRefresh.setOnRefreshListener {
            Log.d("MORALESGO", "Refrescando catálogo desde la nube...")
            cargarProductosDesdeApi()
            binding.imgAvatar.setOnClickListener {
                val intent = android.content.Intent(this, NuevoClienteActivity::class.java)
                startActivity(intent)
            }
        }

        // 3. Configurar los eventos de los Chips de filtrado visual
        configurarFiltrosPorCategoria()

        // 4. Cargar el catálogo por primera vez al abrir la pantalla
        cargarProductosDesdeApi()
    }

    private fun cargarProductosDesdeApi() {
        Log.d("MORALESGO", "Iniciando petición HTTP GET a: $URL_API")

        val jsonArrayRequest = object : JsonArrayRequest(
            Request.Method.GET, URL_API, null,
            { response ->
                // 🔥 APAGAR RUEDITA: La respuesta llegó con éxito
                binding.swipeRefresh.isRefreshing = false

                Log.d("MORALESGO", "✅ Respuesta recibida: ${response.length()} items")
                try {
                    val gson = Gson()
                    val tipoLista = object : TypeToken<List<Producto>>() {}.type

                    // Parseo del JSON limpio anti-bucle del backend
                    val productosDescargados: List<Producto> = gson.fromJson(response.toString(), tipoLista)

                    // Actualizamos nuestras listas en memoria
                    listaCompletaProductos = productosDescargados.toMutableList()

                    // Notificamos al adaptador para pintar los calzados de inmediato
                    productoAdapter.actualizarLista(listaCompletaProductos)

                    // Resetear los chips al estado predeterminado ("Todos")
                    marcarChipActivo(binding.chipTodos)

                    Toast.makeText(this, "Catálogo actualizado", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Log.e("MORALESGO", "❌ Error GSON al parsear: ${e.message}")
                    Toast.makeText(this, "Error de procesamiento de datos", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // 🔥 APAGAR RUEDITA: Detener animación en caso de error de conexión
                binding.swipeRefresh.isRefreshing = false

                Log.e("MORALESGO", "❌ Error de Red Volley: ${error.message}")
                Toast.makeText(this, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getRetryPolicy() = com.android.volley.DefaultRetryPolicy(
                15000, 1, 1.0f
            )
        }
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest)
    }

    private fun configurarFiltrosPorCategoria() {
        // Evento para el Chip "Todos"
        binding.chipTodos.setOnClickListener {
            marcarChipActivo(binding.chipTodos)
            productoAdapter.actualizarLista(listaCompletaProductos)
        }

        // Evento para el Chip "Zapatillas"
        binding.chipZapatillas.setOnClickListener {
            marcarChipActivo(binding.chipZapatillas)
            filtrarCatalogoPorNombreCategoria("Zapatillas")
        }

        // Evento para el Chip "Sandalias"
        binding.chipSandalias.setOnClickListener {
            marcarChipActivo(binding.chipSandalias)
            filtrarCatalogoPorNombreCategoria("Sandalias")
        }

        // Evento para el Chip "Botas"
        binding.chipBotas.setOnClickListener {
            marcarChipActivo(binding.chipBotas)
            filtrarCatalogoPorNombreCategoria("Botas")
        }
    }

    private fun filtrarCatalogoPorNombreCategoria(nombreCategoria: String) {
        // Filtramos la lista completa en milisegundos usando predicados de Kotlin
        val listaFiltrada = listaCompletaProductos.filter {
            it.categoria.nombre.equals(nombreCategoria, ignoreCase = true)
        }

        // Enviamos la sublista filtrada al adaptador
        productoAdapter.actualizarLista(listaFiltrada)

        if (listaFiltrada.isEmpty()) {
            Toast.makeText(this, "No hay calzados en esta categoría", Toast.LENGTH_SHORT).show()
        }
    }

    private fun marcarChipActivo(chipSeleccionado: Chip) {
        // Reseteamos los estilos visuales de todos los chips para que combinen con tu paleta #5B4FFF
        val chips = listOf(binding.chipTodos, binding.chipZapatillas, binding.chipSandalias, binding.chipBotas)

        chips.forEach { chip ->
            if (chip == chipSeleccionado) {
                chip.setChipBackgroundColorResource(android.R.color.transparent)
                chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#5B4FFF"))
                chip.setTextColor(Color.WHITE)
            } else {
                chip.setChipBackgroundColorResource(android.R.color.transparent)
                chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.WHITE)
                chip.setTextColor(Color.parseColor("#5B4FFF"))
            }
        }
    }
}