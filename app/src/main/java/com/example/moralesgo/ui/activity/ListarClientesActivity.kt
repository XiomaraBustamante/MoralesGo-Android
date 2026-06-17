package com.example.moralesgo.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moralesgo.database.AppDatabase
import com.example.moralesgo.databinding.ActivityListarClientesBinding
import com.example.moralesgo.model.entity.Clientes
import com.example.moralesgo.ui.adapter.ClientesAdapterRV
import kotlinx.coroutines.launch

class ListarClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListarClientesBinding
    private lateinit var adapter: ClientesAdapterRV
    private var listaClientes = mutableListOf<Clientes>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListarClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecycler()

        // Jalamos los datos de Room de manera asíncrona al arrancar la pantalla
        lifecycleScope.launch {
            mostrarClientes()
        }

        // Botón de Sincronización (Aquí conectaremos con Volley hacia tu API)
        binding.btnSincronizarNube.setOnClickListener {
            Toast.makeText(this, "Sincronizando clientes con Railway...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarRecycler() {
        adapter = ClientesAdapterRV(listaClientes)
        binding.rvClientes.layoutManager = LinearLayoutManager(this)
        binding.rvClientes.adapter = adapter
    }

    private suspend fun mostrarClientes() {
        listaClientes = AppDatabase.getDatabase(this)
            .clientesdao()
            .ListarClientes()
            .toMutableList()
        adapter.actualizarLista(listaClientes)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            mostrarClientes()
        }
    }
}