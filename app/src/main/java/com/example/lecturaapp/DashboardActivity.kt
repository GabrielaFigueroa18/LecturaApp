package com.example.lecturaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            irAlLogin()
            return
        }

        setContentView(R.layout.activity_dashboard)

        val bienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val etMeta = findViewById<EditText>(R.id.etMeta)
        val tvMeta = findViewById<TextView>(R.id.tvMeta)
        val btnGuardarMeta = findViewById<Button>(R.id.btnGuardarMeta)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        bienvenida.text = "¡Hola, ${auth.currentUser?.email}!"

        val anio = Calendar.getInstance().get(Calendar.YEAR)

        btnGuardarMeta.setOnClickListener {
            val meta = etMeta.text.toString().toIntOrNull()

            if (meta == null || meta <= 0) {
                etMeta.error = "Ingresa un número entero mayor que cero"
                return@setOnClickListener
            }

            tvMeta.text = "Meta $anio: $meta libros"

            Toast.makeText(
                this,
                "Meta establecida temporalmente",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            irAlLogin()
        }
    }

    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
