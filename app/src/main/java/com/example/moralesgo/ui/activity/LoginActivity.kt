package com.example.moralesgo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.moralesgo.R
import com.example.moralesgo.controller.network.VolleySingleton
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    // 🚀 Cambia esto por tu URL real de producción en Railway cuando lo subas
    private val URL_LOGIN = "http://10.0.2.2:8068/api/usuarios/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Mapeo de componentes de tu XML
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etClave = findViewById<EditText>(R.id.etClave)
        val btnIngresar = findViewById<MaterialButton>(R.id.btnIngresar)

        btnIngresar.setOnClickListener {
            val txtUsuario = etUsuario.text.toString().trim()
            val txtClave = etClave.text.toString().trim()

            // 🔥 ACTUALIZADO: Estructura limpia libre de errores de etiquetas
            if (txtUsuario.isEmpty() || txtClave.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Deshabilitamos el botón temporalmente para evitar doble clic
                btnIngresar.isEnabled = false
                btnIngresar.text = "Verificando credenciales..."

                ejecutarLoginEnNube(txtUsuario, txtClave, btnIngresar)
            }
        }
    }

    private fun ejecutarLoginEnNube(usuario: String, clave: String, boton: MaterialButton) {
        // Creamos el cuerpo JSON idéntico a lo que espera recibir tu backend
        val parametros = JSONObject()
        parametros.put("usuario", usuario)
        parametros.put("clave", clave)

        val request = JsonObjectRequest(
            Request.Method.POST, URL_LOGIN, parametros,
            { response ->
                // RESTAURACIÓN DE BOTÓN: El servidor respondió
                boton.isEnabled = true
                boton.text = "Iniciar Sesión"

                try {
                    // Extraemos el estado que viene del backend (1 para activo, 0 para inactivo)
                    val estadoActivo = response.optBoolean("estado", false)

                    if (!estadoActivo) {
                        // 🛑 REGLA DE NEGOCIO: Vendedor Inactivo -> Bloqueado con alerta premium
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Acceso Denegado")
                            .setMessage("Tu cuenta de vendedor se encuentra INACTIVA en el sistema. Comunícate con el Administrador de Calzados Morales.")
                            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
                            .setCancelable(false)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    } else {
                        // Vendedor Activo -> Pasa libremente a la MainActivity del catálogo
                        Toast.makeText(this, "¡Bienvenido, $usuario!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Cerramos el Login de la pila de actividades
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // ERROR DE CONEXIÓN: Restauramos el botón
                boton.isEnabled = true
                boton.text = "Iniciar Sesión"

                val codigoError = error.networkResponse?.statusCode
                if (codigoError == 401 || codigoError == 403) {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error de red: No se pudo conectar al servidor", Toast.LENGTH_LONG).show()
                }
            }
        )

        // Enviamos la petición a la cola global de Volley
        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }
}