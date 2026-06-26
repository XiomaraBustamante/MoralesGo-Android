package com.example.moralesgo.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.moralesgo.R
import com.example.moralesgo.controller.network.VolleySingleton
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private val URL_LOGIN = "http://10.0.2.2:8068/api/usuarios/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etClave = findViewById<EditText>(R.id.etClave)
        val btnIngresar = findViewById<MaterialButton>(R.id.btnIngresar)

        btnIngresar.setOnClickListener {
            val txtUsuario = etUsuario.text.toString().trim()
            val txtClave = etClave.text.toString().trim()


            etUsuario.error = null
            etClave.error = null


            if (txtUsuario.isEmpty()) {
                etUsuario.error = "El usuario es obligatorio para ingresar"
                etUsuario.requestFocus()
                return@setOnClickListener
            }

            if (txtClave.isEmpty()) {
                etClave.error = "La contraseña es obligatoria"
                etClave.requestFocus()
                return@setOnClickListener
            }


            btnIngresar.isEnabled = false
            btnIngresar.text = "Verificando credenciales..."

            ejecutarLoginEnNube(txtUsuario, txtClave, btnIngresar)
        }
    }

    private fun ejecutarLoginEnNube(usuario: String, clave: String, boton: MaterialButton) {
        val parametros = JSONObject()
        parametros.put("usuario", usuario)
        parametros.put("clave", clave)

        val request = JsonObjectRequest(
            Request.Method.POST, URL_LOGIN, parametros,
            { response ->
                boton.isEnabled = true
                boton.text = "Iniciar Sesión"

                try {
                    val estadoActivo = response.optBoolean("estado", false)

                    if (!estadoActivo) {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Acceso Denegado")
                            .setMessage("Tu cuenta de vendedor se encuentra INACTIVA en el sistema. Comunícate con el Administrador de Calzados Morales.")
                            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
                            .setCancelable(false)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    } else {

                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "✨¡Bienvenido de vuelta, $usuario!",
                            Snackbar.LENGTH_LONG
                        ).setBackgroundTint(Color.parseColor("#4F46E5"))
                            .setTextColor(Color.WHITE)
                            .show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                } catch (e: Exception) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Error de Procesamiento")
                        .setMessage("Ocurrió un inconveniente al interpretar la respuesta de seguridad.")
                        .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            },
            { error ->
                boton.isEnabled = true
                boton.text = "Iniciar Sesión"

                val codigoError = error.networkResponse?.statusCode
                if (codigoError == 401 || codigoError == 403) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Error de Autenticación")
                        .setMessage("El usuario o la contraseña ingresados son incorrectos, o la cuenta no existe en Calzados Morales.")
                        .setPositiveButton("Reintentar") { dialog, _ -> dialog.dismiss() }
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Error de Conexión")
                        .setMessage("No se pudo establecer comunicación con el servidor en la nube. Verifique su conexión a internet.")
                        .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show()
                }
            }
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }
}