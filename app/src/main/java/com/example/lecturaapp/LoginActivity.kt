
package com.example.lecturaapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        // Inicializar Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Conectar elementos del XML
        val correo = findViewById<EditText>(R.id.etCorreo)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegistro = findViewById<TextView>(R.id.tvRegistro)

        // BOTÓN INICIAR SESIÓN
        btnLogin.setOnClickListener {

            val email = correo.text.toString().trim()
            val clave = password.text.toString()

            // Validar correo electrónico
            if (email.isEmpty()) {
                correo.error = "Ingresa tu correo electrónico"
                correo.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                correo.error = "Ingresa un correo válido"
                correo.requestFocus()
                return@setOnClickListener
            }

            // Validar contraseña
            if (clave.isEmpty()) {
                password.error = "Ingresa tu contraseña"
                password.requestFocus()
                return@setOnClickListener
            }

            if (clave.length < 6) {
                password.error = "Mínimo 6 caracteres"
                password.requestFocus()
                return@setOnClickListener
            }

            // Evitar múltiples clics
            btnLogin.isEnabled = false

            // Iniciar sesión con Firebase
            auth.signInWithEmailAndPassword(email, clave)
                .addOnCompleteListener(this) { task ->

                    btnLogin.isEnabled = true

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Inicio de sesión exitoso",
                            Toast.LENGTH_SHORT
                        ).show()

                        abrirDashboard()

                    } else {

                        Toast.makeText(
                            this,
                            "Error al iniciar sesión: " +
                                    (task.exception?.localizedMessage
                                        ?: "Verifica tus datos"),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // IR A REGISTRO
        tvRegistro.setOnClickListener {

            val intent = Intent(
                this,
                RegistroActivity::class.java
            )

            startActivity(intent)
        }
    }

    // COMPROBAR SI YA EXISTE UNA SESIÓN
    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null) {
            abrirDashboard()
        }
    }

    // ABRIR DASHBOARD
    private fun abrirDashboard() {

        val intent = Intent(
            this,
            DashboardActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
