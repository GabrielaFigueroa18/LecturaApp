
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

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()

        val correo = findViewById<EditText>(
            R.id.etCorreoRegistro
        )

        val password = findViewById<EditText>(
            R.id.etPasswordRegistro
        )

        val confirmar = findViewById<EditText>(
            R.id.etConfirmarPassword
        )

        val btnRegistrar = findViewById<Button>(
            R.id.btnRegistrar
        )

        val tvIrLogin = findViewById<TextView>(
            R.id.tvIrLogin
        )

        btnRegistrar.setOnClickListener {

            val email = correo.text.toString().trim()
            val clave = password.text.toString()
            val confirmacion = confirmar.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                correo.error = "Ingresa un correo válido"
                return@setOnClickListener
            }

            if (clave.length < 6) {
                password.error = "Mínimo 6 caracteres"
                return@setOnClickListener
            }

            if (clave != confirmacion) {
                confirmar.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            btnRegistrar.isEnabled = false

            auth.createUserWithEmailAndPassword(email, clave)
                .addOnCompleteListener(this) { task ->

                    btnRegistrar.isEnabled = true

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Cuenta creada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(
                            this,
                            DashboardActivity::class.java
                        )

                        startActivity(intent)
                        finish()

                    } else {

                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        tvIrLogin.setOnClickListener {
            finish()
        }
    }
}
