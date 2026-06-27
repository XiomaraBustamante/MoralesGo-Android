package com.example.moralesgo.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.example.moralesgo.controller.network.VolleySingleton
import com.example.moralesgo.databinding.ActivityHistorialVentasBinding
import com.example.moralesgo.model.entity.VentaHistorial
import com.example.moralesgo.ui.adapter.VentaAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistorialVentasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialVentasBinding
    private lateinit var adapter: VentaAdapter


    private val listaVentas = mutableListOf<VentaHistorial>()

    private val listaFiltrada = mutableListOf<VentaHistorial>()


    private val URL_LISTAR_VENTAS = "https://calzadosmorales-backend-production.up.railway.app/api/ventas/listar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialVentasBinding.inflate(layoutInflater)
        setContentView(binding.root)


        adapter = VentaAdapter(listaFiltrada)
        binding.rvHistorialVentas.layoutManager = LinearLayoutManager(this)
        binding.rvHistorialVentas.adapter = adapter


        binding.btnAtrasHistorialCard.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        configurarBuscador()


        obtenerHistorialVentasCloud()
    }

    private fun obtenerHistorialVentasCloud() {
        binding.progressHistorial.visibility = View.VISIBLE
        binding.lytEmptyStateVentas.visibility = View.GONE

        lifecycleScope.launch {
            val jsonArrayRequest = JsonArrayRequest(
                Request.Method.GET, URL_LISTAR_VENTAS, null,
                { response ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            listaVentas.clear()
                            for (i in 0 until response.length()) {
                                val obj = response.getJSONObject(i)

                                val venta = VentaHistorial(
                                    idVenta = obj.getInt("id_venta"),
                                    clienteNombre = obj.getString("cliente_nombre"),
                                    montoTotal = obj.getDouble("monto_total"),
                                    fecha = obj.getString("fecha_registro"),
                                    numeroBoleta = obj.getString("numero_boleta")
                                )
                                listaVentas.add(venta)
                            }


                            withContext(Dispatchers.Main) {
                                listaFiltrada.clear()
                                listaFiltrada.addAll(listaVentas)
                                adapter.actualizarLista(listaFiltrada)

                                binding.progressHistorial.visibility = View.GONE
                                verificarEstadoLista()
                            }

                        } catch (e: Exception) {
                            Log.e("MORALESGO", "Error de parseo JSON: ${e.message}")
                            withContext(Dispatchers.Main) {
                                binding.progressHistorial.visibility = View.GONE
                            }
                        }
                    }
                },
                { error ->
                    Log.e("MORALESGO", "Error HTTP Volley: ${error.message}")
                    binding.progressHistorial.visibility = View.GONE
                    Toast.makeText(
                        this@HistorialVentasActivity,
                        "Error al conectar con el servidor central",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
            VolleySingleton.getInstance(this@HistorialVentasActivity).addToRequestQueue(jsonArrayRequest)
        }
    }

    private fun configurarBuscador() {
        binding.etBuscarVenta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarVentas(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarVentas(texto: String) {
        listaFiltrada.clear()

        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaVentas)
        } else {
            val query = texto.lowercase().trim()
            for (venta in listaVentas) {

                if (venta.numeroBoleta.lowercase().contains(query) ||
                    venta.clienteNombre.lowercase().contains(query)) {
                    listaFiltrada.add(venta)
                }
            }
        }
        adapter.actualizarLista(listaFiltrada)
        verificarEstadoLista()
    }

    private fun verificarEstadoLista() {
        if (listaFiltrada.isEmpty()) {
            binding.lytEmptyStateVentas.visibility = View.VISIBLE
            binding.rvHistorialVentas.visibility = View.GONE
        } else {
            binding.lytEmptyStateVentas.visibility = View.GONE
            binding.rvHistorialVentas.visibility = View.VISIBLE
        }
    }
}