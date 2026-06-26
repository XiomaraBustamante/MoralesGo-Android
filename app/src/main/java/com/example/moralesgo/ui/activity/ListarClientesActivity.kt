package com.example.moralesgo.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moralesgo.database.AppDatabase
import com.example.moralesgo.databinding.ActivityListarClientesBinding
import com.example.moralesgo.model.entity.Clientes
import com.example.moralesgo.ui.adapter.ClientesAdapterRV
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ListarClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListarClientesBinding
    private lateinit var adapter: ClientesAdapterRV

    private var listaClientesVisibles = mutableListOf<Clientes>()
    private var listaClientesCompleta = mutableListOf<Clientes>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListarClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAtrasClientesCard.setOnClickListener { finish() }
        binding.btnAtrasClientes.setOnClickListener { finish() }

        configurarRecycler()
        configurarBuscador()

        lifecycleScope.launch {
            mostrarClientes()
        }

        binding.fabNuevoCliente.setOnClickListener {
            val intent = Intent(this, NuevoClienteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configurarBuscador() {
        binding.etBuscarCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarDirectorioLocal(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarDirectorioLocal(texto: String) {
        val palabraClave = texto.lowercase(Locale.getDefault()).trim()

        if (palabraClave.isEmpty()) {
            listaClientesVisibles = listaClientesCompleta.toMutableList()
        } else {
            listaClientesVisibles = listaClientesCompleta.filter { cliente ->
                val nombreCompleto = "${cliente.nombre} ${cliente.apellido}".lowercase(Locale.getDefault())
                val razonSocial = (cliente.razonSocial ?: "").lowercase(Locale.getDefault())
                val dni = cliente.dni ?: ""
                val ruc = cliente.ruc ?: ""
                val telefono = cliente.telefono ?: ""

                nombreCompleto.contains(palabraClave) ||
                        razonSocial.contains(palabraClave) ||
                        dni.contains(palabraClave) ||
                        ruc.contains(palabraClave) ||
                        telefono.contains(palabraClave)
            }.toMutableList()
        }

        adapter.actualizarLista(listaClientesVisibles)
        actualizarEstadoVacioVisual()
    }

    private fun configurarRecycler() {
        adapter = ClientesAdapterRV(
            listaClientes = listaClientesVisibles,
            onClienteClick = { clienteSeleccionado ->
                val nombreFinal = if (clienteSeleccionado.tipoCliente == 1) {
                    "${clienteSeleccionado.nombre} ${clienteSeleccionado.apellido}"
                } else {
                    clienteSeleccionado.razonSocial ?: "Empresa sin Razón Social"
                }

                val resultadoIntent = Intent().apply {
                    putExtra("CLIENTE_ID", clienteSeleccionado.idCliente)
                    putExtra("CLIENTE_NOMBRE", nombreFinal)
                }

                setResult(Activity.RESULT_OK, resultadoIntent)
                finish()
            },
            onEditarClick = { clienteAEditar ->
                val intent = Intent(this, NuevoClienteActivity::class.java).apply {
                    putExtra("KEY_CLIENTE_ID", clienteAEditar.idCliente)
                }
                startActivity(intent)
            },
            onEliminarClick = { clienteAEliminar ->
                val nombreCliente = if (clienteAEliminar.tipoCliente == 1) {
                    "${clienteAEliminar.nombre} ${clienteAEliminar.apellido}"
                } else {
                    clienteAEliminar.razonSocial ?: "Empresa"
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle("⚠️ ¿Eliminar Registro Local?")
                    .setMessage("¿Está seguro de quitar a:\n👉 $nombreCliente\ndel almacenamiento interno de la aplicación?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Eliminar") { _, _ ->
                        ejecutarEliminacionDeCliente(clienteAEliminar)
                    }
                    .show()
            }
        )

        binding.rvClientes.layoutManager = LinearLayoutManager(this)
        binding.rvClientes.adapter = adapter
    }

    private fun ejecutarEliminacionDeCliente(cliente: Clientes) {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            database.clientesdao().EliminarCliente(cliente)

            withContext(Dispatchers.Main) {
                Snackbar.make(binding.root, "🗑️ Registro eliminado del almacenamiento local.", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#1E1B24"))
                    .setTextColor(Color.WHITE)
                    .show()
                mostrarClientes()
            }
        }
    }

    private suspend fun mostrarClientes() {
        listaClientesCompleta = AppDatabase.getDatabase(this)
            .clientesdao()
            .ListarClientes()
            .toMutableList()

        val queryActual = binding.etBuscarCliente.text.toString()
        if (queryActual.isNotEmpty()) {
            filtrarDirectorioLocal(queryActual)
        } else {
            listaClientesVisibles = listaClientesCompleta.toMutableList()
            adapter.actualizarLista(listaClientesVisibles)
            actualizarEstadoVacioVisual()
        }
    }

    private fun actualizarEstadoVacioVisual() {
        if (listaClientesVisibles.isEmpty()) {
            binding.rvClientes.visibility = View.GONE
            binding.lytEmptyStateClientes.visibility = View.VISIBLE
        } else {
            binding.rvClientes.visibility = View.VISIBLE
            binding.lytEmptyStateClientes.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            mostrarClientes()
        }
    }
}