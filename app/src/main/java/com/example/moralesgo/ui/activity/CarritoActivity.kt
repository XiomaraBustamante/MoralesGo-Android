package com.example.moralesgo.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.moralesgo.controller.network.VolleySingleton
import com.example.moralesgo.database.AppDatabase
import com.example.moralesgo.databinding.ActivityCarritoBinding
import com.example.moralesgo.model.entity.CarritoLocal
import com.example.moralesgo.ui.adapter.CarritoAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var adapter: CarritoAdapter

    private var idClienteSeleccionado: Int? = null
    private var textClienteNombre: String? = null

    private val URL_GUARDAR_VENTA = "https://calzadosmorales-backend-production.up.railway.app/api/ventas/guardar"

    private val iniciarSeleccionClienteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                idClienteSeleccionado = data.getIntExtra("CLIENTE_ID", -1)
                val nombreCli = data.getStringExtra("CLIENTE_NOMBRE")

                if (idClienteSeleccionado != null && idClienteSeleccionado != -1) {
                    binding.tvClienteSeleccionado.text = "Cliente: $nombreCli"
                    binding.tvClienteSeleccionado.setTextColor(Color.parseColor("#1E1B24"))

                    autoseleccionarComprobantePorTipo()
                    validarEstadoBotonProcesar()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewCarrito.layoutManager = LinearLayoutManager(this)

        binding.btnAtrasCarritoCard.setOnClickListener { finish() }
        binding.btnAtrasCarrito.setOnClickListener { finish() }

        configurarSelectoresFacturacion()
        cargarCarritoLocal()

        binding.btnSeleccionarCliente.setOnClickListener {
            val intent = Intent(this, ListarClientesActivity::class.java)
            iniciarSeleccionClienteLauncher.launch(intent)
        }

        binding.btnProcesarCompra.setOnClickListener {
            procesarYEnviarVentaALaNube()
        }
    }

    private fun configurarSelectoresFacturacion() {
        val opcionesPago = arrayOf("Efectivo", "Yape", "Plin")
        val adapterPago = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesPago)
        binding.spnMetodoPago.adapter = adapterPago

        val opcionesComprobante = arrayOf("Boleta", "Factura")
        val adapterComprobante = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesComprobante)
        binding.spnComprobante.adapter = adapterComprobante
    }

    private fun autoseleccionarComprobantePorTipo() {
        if (idClienteSeleccionado == null) return
        val database = AppDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val clienteLocal = database.clientesdao().ObtenerClientePorId(idClienteSeleccionado!!)
            withContext(Dispatchers.Main) {
                if (clienteLocal != null) {
                    if (clienteLocal.tipoCliente == 1) {
                        binding.spnComprobante.setSelection(0)
                    } else {
                        binding.spnComprobante.setSelection(1)
                    }
                }
            }
        }
    }


    private fun cargarCarritoLocal() {
        val database = AppDatabase.getDatabase(this)
        val carritoDao = database.carritodao()

        lifecycleScope.launch(Dispatchers.IO) {
            val listaLocal = carritoDao.obtenerCarrito()


            for (item in listaLocal) {
                item.subtotal = item.precio * item.cantidad.toDouble()
            }

            withContext(Dispatchers.Main) {
                adapter = CarritoAdapter(
                    listaLocal,
                    { item -> eliminarItem(item) },
                    { item -> mostrarDialogoEditarCantidad(item) }
                )
                binding.recyclerViewCarrito.adapter = adapter

                val totalGeneral: Double = listaLocal.sumOf { it.precio * it.cantidad.toDouble() }
                val subtotal = totalGeneral / 1.18
                val igv = totalGeneral - subtotal

                binding.tvSubtotalVenta.text = "S/ %.2f".format(subtotal)
                binding.tvIgvVenta.text = "S/ %.2f".format(igv)
                binding.tvTotalVenta.text = "S/ %.2f".format(totalGeneral)

                validarEstadoBotonProcesar()
            }
        }
    }


    private fun mostrarDialogoEditarCantidad(item: CarritoLocal) {
        val contextActivity = this@CarritoActivity
        val inputEditText = EditText(contextActivity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(item.cantidad.toString())
            setSelection(text.length)
        }

        MaterialAlertDialogBuilder(contextActivity)
            .setTitle("📝Editar Cantidad")
            .setMessage("Modifique las unidades para el modelo:\n${item.nombre} (Talla: ${item.tallaNombre})")
            .setView(inputEditText)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevaCantidadStr = inputEditText.text.toString().trim()
                if (nuevaCantidadStr.isNotEmpty()) {
                    val nuevaCantidad = nuevaCantidadStr.toInt()
                    if (nuevaCantidad > 0) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            item.companionUpdate(nuevaCantidad)
                            AppDatabase.getDatabase(contextActivity).carritodao().actualizarItem(item)
                            withContext(Dispatchers.Main) {
                                cargarCarritoLocal()
                                Snackbar.make(binding.root, "Cantidad actualizada.", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        eliminarItem(item)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun CarritoLocal.companionUpdate(nuevaCantidad: Int) {
        this.cantidad = nuevaCantidad
        this.subtotal = this.precio * nuevaCantidad.toDouble()
    }

    private fun validarEstadoBotonProcesar() {
        val tieneProductos = ::adapter.isInitialized && adapter.itemCount > 0
        val tieneCliente = idClienteSeleccionado != null && idClienteSeleccionado != -1

        if (tieneProductos && tieneCliente) {
            binding.btnProcesarCompra.isEnabled = true
            binding.btnProcesarCompra.text = "CONFIRMAR Y FACTURAR"
            binding.btnProcesarCompra.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#5B4FFF"))
        } else {
            binding.btnProcesarCompra.isEnabled = false
            binding.btnProcesarCompra.text = "CONFIRMAR Y FACTURAR"
            binding.btnProcesarCompra.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#A5A1B2"))
        }
    }

    private fun procesarYEnviarVentaALaNube() {
        if (idClienteSeleccionado == null || idClienteSeleccionado == -1) {
            MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Asociación Requerida")
                .setMessage("Debe vincular un cliente activo a la orden antes de facturar la venta.")
                .setPositiveButton("Vincular Ahora") { _, _ -> binding.btnSeleccionarCliente.performClick() }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }

        binding.btnProcesarCompra.isEnabled = false
        binding.btnProcesarCompra.text = "PROCESANDO..."
        binding.btnProcesarCompra.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#A5A1B2"))

        val database = AppDatabase.getDatabase(this)
        val metodoPagoSeleccionado = binding.spnMetodoPago.selectedItem.toString()
        val tipoComprobanteSeleccionado = binding.spnComprobante.selectedItem.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            val itemsCarrito = database.carritodao().obtenerCarrito()
            val clienteLocal = database.clientesdao().ObtenerClientePorId(idClienteSeleccionado!!)

            if (itemsCarrito.isEmpty() || clienteLocal == null) {
                withContext(Dispatchers.Main) { validarEstadoBotonProcesar() }
                return@launch
            }

            val jsonBody = JSONObject()
            jsonBody.put("tipo_comprobante", tipoComprobanteSeleccionado)
            jsonBody.put("metodo_pago", metodoPagoSeleccionado)
            jsonBody.put("id_usuario", 2)

            val uuidTransaccional = UUID.randomUUID().toString()
            jsonBody.put("codigo_sincronizacion", uuidTransaccional)

            val jsonCliente = JSONObject()
            jsonCliente.put("nombre", if (clienteLocal.tipoCliente == 1) clienteLocal.nombre else clienteLocal.razonSocial)
            jsonCliente.put("num_documento", if (clienteLocal.tipoCliente == 1) clienteLocal.dni else clienteLocal.ruc)
            jsonCliente.put("telefono", clienteLocal.telefono)
            jsonCliente.put("direccion", clienteLocal.direccion)
            jsonCliente.put("email", clienteLocal.email)

            if (clienteLocal.tipoCliente == 1) {
                jsonCliente.put("genero", clienteLocal.genero)
                jsonCliente.put("apellido", clienteLocal.apellido)
            } else {
                jsonCliente.put("repre_legal", clienteLocal.repreLegal)
            }
            jsonBody.put("cliente", jsonCliente)

            val jsonArrayDetalles = JSONArray()
            for (item in itemsCarrito) {
                val jsonItem = JSONObject()
                jsonItem.put("id_producto", item.idProducto)
                jsonItem.put("id_talla", item.idTalla)
                jsonItem.put("cantidad", item.cantidad)
                jsonItem.put("precio", item.precio)
                jsonArrayDetalles.put(jsonItem)
            }
            jsonBody.put("detalles", jsonArrayDetalles)

            withContext(Dispatchers.Main) {
                enviarPeticionServidor(jsonBody, database)
            }
        }
    }

    private fun enviarPeticionServidor(jsonBody: JSONObject, database: AppDatabase) {
        Log.d("MORALESGO", "Enviando JSON de Venta: $jsonBody")

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, URL_GUARDAR_VENTA, jsonBody,
            { response ->
                Log.d("MORALESGO", "✅Venta guardada con éxito: $response")
                try {
                    val numeroComprobante = response.optString("numero_boleta", "000000")

                    MaterialAlertDialogBuilder(this)
                        .setTitle("🎉 ¡Venta Confirmada!")
                        .setMessage("La venta se procesó y registró correctamente en la nube.\nComprobante: $numeroComprobante")
                        .setPositiveButton("Aceptar") { _, _ ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    database.carritodao().vaciarCarrito()
                                    withContext(Dispatchers.Main) {
                                        cargarCarritoLocal()
                                        finish()
                                    }
                                } catch (e: Exception) {
                                    Log.e("MORALESGO", "Error al limpiar base de datos local: ${e.message}")
                                    withContext(Dispatchers.Main) { finish() }
                                }
                            }
                        }
                        .setCancelable(false)
                        .show()
                } catch (e: Exception) {
                    Log.e("MORALESGO", "Error al procesar el diálogo de éxito: ${e.message}")
                    validarEstadoBotonProcesar()
                }
            },
            { error ->
                Log.e("MORALESGO", "Error al facturar: ${error.message}")
                validarEstadoBotonProcesar()

                val mensajeMapeado = when {
                    error is com.android.volley.TimeoutError -> "El servidor de Calzados Morales tardó demasiado en responder. Verifique su conexión de red o el panel web antes de reintentar."
                    error.networkResponse != null && error.networkResponse.statusCode == 409 -> "Esta orden ya fue asentada en el servidor central (Idempotencia móvil)."
                    else -> "No se pudo registrar la venta en el servidor central. Intente nuevamente."
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle("📡 Alerta de Comunicación")
                    .setMessage(mensajeMapeado)
                    .setPositiveButton("Reintentar") { _, _ -> procesarYEnviarVentaALaNube() }
                    .setNegativeButton("Entendido", null)
                    .show()
            }
        ) {
            override fun getRetryPolicy(): com.android.volley.RetryPolicy {
                return DefaultRetryPolicy(
                    15000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun eliminarItem(item: CarritoLocal) {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            database.carritodao().eliminarItem(item)
            withContext(Dispatchers.Main) {
                cargarCarritoLocal()
            }
        }
    }
}