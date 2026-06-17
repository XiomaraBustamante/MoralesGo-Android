package com.example.moralesgo.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moralesgo.database.AppDatabase
import com.example.moralesgo.databinding.ActivityNuevoClienteBinding
import com.example.moralesgo.model.entity.Clientes
import kotlinx.coroutines.launch

class NuevoClienteActivity : AppCompatActivity() {

    // Utilización de ViewBinding oficial de tu clase
    private lateinit var binding: ActivityNuevoClienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflamos la vista mediante Binding
        binding = ActivityNuevoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Acción del botón para cerrar la actividad y regresar al menú [cite: 75]
        binding.btnRegresar.setOnClickListener {
            finish()
        }

        // Acción del botón para procesar la persistencia local [cite: 75]
        binding.btnGrabar.setOnClickListener {
            val txtId = binding.edtid.text.toString().trim()
            val txtNombre = binding.edtnomcli.text.toString().trim()
            val txtCredito = binding.edtcredito.text.toString().trim()
            val txtFecha = binding.edtfechareg.text.toString().trim()

            // Validaciones básicas de negocio en el campo
            if (txtId.isEmpty() || txtNombre.isEmpty() || txtCredito.isEmpty() || txtFecha.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos del formulario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            grabarClienteEnSQLite(
                txtId.toInt(),
                txtNombre,
                txtCredito.toDoubleOrNull() ?: 0.0,
                txtFecha
            )
        }
    }

    private fun grabarClienteEnSQLite(codigo: Int, nombre: String, credito: Double, fecha: String) {
        // Instanciamos el data class estructural de tu modelo Room [cite: 80]
        val nuevoCliente = Clientes(
            id = codigo,
            nomcli = nombre,
            credito = credito,
            fechareg = fecha
        )

        // 🚀 TEMA 7 DEL SÍLABO: Ejecución en segundo plano mediante Corrutinas
        lifecycleScope.launch {
            try {
                // Inserción síncrona en el archivo SQLite Empresa.db mediante el DAO [cite: 81, 17]
                AppDatabase.getDatabase(applicationContext)
                    .clientesdao()
                    .AgregarCliente(nuevoCliente)

                Toast.makeText(applicationContext, "¡Cliente guardado localmente en SQLite!", Toast.LENGTH_LONG).show()

                // Limpiamos el formulario para un nuevo ingreso
                binding.edtid.text?.clear()
                binding.edtnomcli.text?.clear()
                binding.edtcredito.text?.clear()
                binding.edtfechareg.text?.clear()

                finish() // Regresa automáticamente al listado o pantalla anterior

            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Error: El código o DNI ya se encuentra registrado", Toast.LENGTH_LONG).show()
            }
        }
    }
}