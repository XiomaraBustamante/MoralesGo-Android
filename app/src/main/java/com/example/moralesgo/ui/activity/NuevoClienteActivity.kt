package com.example.moralesgo.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moralesgo.database.AppDatabase
import com.example.moralesgo.databinding.ActivityNuevoClienteBinding
import com.example.moralesgo.model.entity.Clientes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NuevoClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoClienteBinding
    private var tipoClienteSeleccionado: Int = 1
    private var fechaRegistroActual: String = ""


    private var clienteIdEdicion: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityNuevoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarSpinners()
        inicializarFechaAutomatica()


        verificarModoEdicion()

        binding.btnAtrasNuevoClienteCard.setOnClickListener { finish() }
        binding.btnAtrasNuevoCliente.setOnClickListener { finish() }

        binding.btnGrabar.setOnClickListener {
            procesarFormulario()
        }
    }

    private fun inicializarFechaAutomatica() {
        fechaRegistroActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun verificarModoEdicion() {
        if (intent.hasExtra("KEY_CLIENTE_ID")) {
            clienteIdEdicion = intent.getIntExtra("KEY_CLIENTE_ID", -1)


            binding.btnGrabar.text = "ACTUALIZAR DATOS DEL CLIENTE"


            (binding.lytHeader.getChildAt(1) as? android.widget.LinearLayout)?.let { layoutTexto ->
                (layoutTexto.getChildAt(0) as? android.widget.TextView)?.text = "Actualizar Cliente"
                (layoutTexto.getChildAt(1) as? android.widget.TextView)?.text = "Modificar datos de registro"
            }

            lifecycleScope.launch {
                val cliente = AppDatabase.getDatabase(this@NuevoClienteActivity)
                    .clientesdao()
                    .ObtenerClientePorId(clienteIdEdicion)

                cliente?.let { rellenarFormularioConData(it) }
            }
        }
    }

    private fun rellenarFormularioConData(cliente: Clientes) {
        // Campos de contacto e información general
        binding.edtDireccion.setText(cliente.direccion)
        binding.edtTelefono.setText(cliente.telefono)
        binding.edtEmail.setText(cliente.email)
        fechaRegistroActual = cliente.fechareg ?: ""

        if (cliente.tipoCliente == 1) {
            binding.spnTipoCliente.setSelection(0) // Forzar visualización de Persona Natural
            binding.edtDni.setText(cliente.dni)
            binding.edtNombre.setText(cliente.nombre)
            binding.edtApellido.setText(cliente.apellido)
            val posGenero = if (cliente.genero == 1) 0 else 1
            binding.spnGenero.setSelection(posGenero)
        } else {
            binding.spnTipoCliente.setSelection(1) // Forzar visualización de Persona Jurídica
            binding.edtRuc.setText(cliente.ruc)
            binding.edtRazonSocial.setText(cliente.razonSocial)
            binding.edtRepreLegal.setText(cliente.repreLegal)
        }
    }

    private fun configurarSpinners() {
        val opcionesTipo = listOf("Persona Natural", "Persona Jurídica")
        val adapterTipo = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesTipo)
        binding.spnTipoCliente.adapter = adapterTipo

        binding.spnTipoCliente.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                limpiarTodosLosErroresVisuales()


                if (clienteIdEdicion == -1) {
                    binding.edtDireccion.setText("")
                    binding.edtTelefono.setText("")
                    binding.edtEmail.setText("")
                    binding.edtDni.setText("")
                    binding.edtNombre.setText("")
                    binding.edtApellido.setText("")
                    binding.edtRuc.setText("")
                    binding.edtRazonSocial.setText("")
                    binding.edtRepreLegal.setText("")
                }

                if (position == 0) {
                    tipoClienteSeleccionado = 1
                    binding.lytPersonaNatural.visibility = View.VISIBLE
                    binding.lytPersonaJuridica.visibility = View.GONE
                } else {
                    tipoClienteSeleccionado = 2
                    binding.lytPersonaNatural.visibility = View.GONE
                    binding.lytPersonaJuridica.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val opcionesGenero = listOf("Masculino", "Femenino")
        val adapterGenero = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesGenero)
        binding.spnGenero.adapter = adapterGenero
    }

    private fun procesarFormulario() {
        limpiarTodosLosErroresVisuales()

        val txtDireccion = binding.edtDireccion.text.toString().trim()
        val txtTelefono = binding.edtTelefono.text.toString().trim()
        val txtEmail = binding.edtEmail.text.toString().trim()
        val txtFechaReg = fechaRegistroActual

        var nuevoCliente: Clientes? = null

        if (tipoClienteSeleccionado == 1) {
            val txtDni = binding.edtDni.text.toString().trim()
            val txtNombre = binding.edtNombre.text.toString().trim()
            val txtApellido = binding.edtApellido.text.toString().trim()
            val intGenero = if (binding.spnGenero.selectedItemPosition == 0) 1 else 2

            if (txtDni.length != 8) {
                binding.tilDni.error = "El DNI de la persona debe constar de 8 dígitos estrictos"
                binding.edtDni.requestFocus()
                return
            }
            if (txtNombre.isEmpty()) {
                binding.tilNombre.error = "Escriba el nombre del cliente"
                binding.edtNombre.requestFocus()
                return
            }
            if (txtApellido.isEmpty()) {
                binding.tilApellido.error = "Escriba el apellido del cliente"
                binding.edtApellido.requestFocus()
                return
            }

            nuevoCliente = Clientes(
                idCliente = if (clienteIdEdicion != -1) clienteIdEdicion else 0,
                tipoCliente = 1,
                direccion = txtDireccion,
                telefono = txtTelefono,
                email = txtEmail,
                fechareg = txtFechaReg,
                dni = txtDni,
                nombre = txtNombre,
                apellido = txtApellido,
                genero = intGenero
            )

        } else {
            val txtRuc = binding.edtRuc.text.toString().trim()
            val txtRazonSocial = binding.edtRazonSocial.text.toString().trim()
            val txtRepreLegal = binding.edtRepreLegal.text.toString().trim()

            if (txtRuc.length != 11) {
                binding.tilRuc.error = "El RUC de la empresa debe constar de 11 dígitos estrictos"
                binding.edtRuc.requestFocus()
                return
            }
            if (txtRazonSocial.isEmpty()) {
                binding.tilRazonSocial.error = "Escriba la razón social de la empresa"
                binding.edtRazonSocial.requestFocus()
                return
            }
            if (txtRepreLegal.isEmpty()) {
                binding.tilRepreLegal.error = "Escriba el nombre del representante legal"
                binding.edtRepreLegal.requestFocus()
                return
            }

            nuevoCliente = Clientes(
                idCliente = if (clienteIdEdicion != -1) clienteIdEdicion else 0,
                tipoCliente = 2,
                direccion = txtDireccion,
                telefono = txtTelefono,
                email = txtEmail,
                fechareg = txtFechaReg,
                ruc = txtRuc,
                razonSocial = txtRazonSocial,
                repreLegal = txtRepreLegal
            )
        }

        if (txtDireccion.isEmpty()) {
            binding.tilDireccion.error = "La dirección de entrega/fiscal es obligatoria"
            binding.edtDireccion.requestFocus()
            return
        }
        if (txtTelefono.length != 9) {
            binding.tilTelefono.error = "El teléfono móvil en Perú debe contener 9 dígitos"
            binding.edtTelefono.requestFocus()
            return
        }
        if (txtEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()) {
            binding.tilEmail.error = "Ingrese una estructura de correo electrónico válida (ejemplo@dominio.com)"
            binding.edtEmail.requestFocus()
            return
        }

        nuevoCliente?.let { grabarEnRoom(it) }
    }

    private fun limpiarTodosLosErroresVisuales() {
        binding.tilDni.error = null
        binding.tilNombre.error = null
        binding.tilApellido.error = null
        binding.tilRuc.error = null
        binding.tilRazonSocial.error = null
        binding.tilRepreLegal.error = null
        binding.tilDireccion.error = null
        binding.tilTelefono.error = null
        binding.tilEmail.error = null
    }

    private fun grabarEnRoom(cliente: Clientes) {
        lifecycleScope.launch {
            try {
                val dao = AppDatabase.getDatabase(applicationContext).clientesdao()


                if (clienteIdEdicion != -1) {
                    dao.ActualizarCliente(cliente)
                } else {
                    dao.AgregarCliente(cliente)
                }

                MaterialAlertDialogBuilder(this@NuevoClienteActivity)
                    .setTitle("🎉 Operación Exitosa")
                    .setMessage(if (clienteIdEdicion != -1) "Los datos del cliente se actualizaron de forma correcta." else "El cliente ha sido guardado de forma correcta.")
                    .setPositiveButton("Aceptar") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()

            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error al persistir el cliente en Room.", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#E53935"))
                    .setTextColor(Color.WHITE)
                    .show()
            }
        }
    }
}