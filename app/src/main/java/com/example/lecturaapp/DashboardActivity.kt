
package com.example.lecturaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var librosRef: DatabaseReference

    private lateinit var etMeta: EditText
    private lateinit var tvMeta: TextView
    private lateinit var tvTerminados: TextView
    private lateinit var tvFaltantes: TextView
    private lateinit var tvLeyendo: TextView
    private lateinit var progresoMeta: ProgressBar

    private var metaActual = 0
    private var librosTerminados = 0

    private val anioActual =
        Calendar.getInstance().get(Calendar.YEAR).toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val usuario = auth.currentUser

        if (usuario == null) {
            irAlLogin()
            return
        }

        setContentView(R.layout.activity_dashboard)

        // CONEXIÓN A FIREBASE
        val firebaseDatabase = FirebaseDatabase.getInstance(
            "https://lecturaapp-78791-default-rtdb.firebaseio.com"
        )

        val usuarioRef = firebaseDatabase
            .getReference("usuarios")
            .child(usuario.uid)

        database = usuarioRef
            .child("metas")
            .child(anioActual)

        librosRef = usuarioRef.child("libros")

        // ELEMENTOS DEL DASHBOARD
        val bienvenida =
            findViewById<TextView>(R.id.tvBienvenida)

        etMeta = findViewById(R.id.etMeta)
        tvMeta = findViewById(R.id.tvMeta)
        tvTerminados = findViewById(R.id.tvTerminados)
        tvFaltantes = findViewById(R.id.tvFaltantes)
        tvLeyendo = findViewById(R.id.tvLeyendo)
        progresoMeta = findViewById(R.id.progresoMeta)

        val btnGuardarMeta =
            findViewById<Button>(R.id.btnGuardarMeta)

        val btnAgregarLibro =
            findViewById<Button>(R.id.btnAgregarLibro)

        val btnCerrarSesion =
            findViewById<Button>(R.id.btnCerrarSesion)

        // MOSTRAR USUARIO
        bienvenida.text = "¡Hola, ${usuario.email}!"

        // CARGAR INFORMACIÓN
        cargarMeta()
        cargarLibros()

        // GUARDAR META
        btnGuardarMeta.setOnClickListener {

            val meta = etMeta.text.toString().toIntOrNull()

            if (meta == null || meta <= 0) {
                etMeta.error =
                    "Ingresa un número entero mayor que cero"
                return@setOnClickListener
            }

            btnGuardarMeta.isEnabled = false

            database.setValue(meta)
                .addOnSuccessListener {

                    btnGuardarMeta.isEnabled = true

                    Toast.makeText(
                        this,
                        "Meta guardada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { error ->

                    btnGuardarMeta.isEnabled = true

                    Toast.makeText(
                        this,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // BOTÓN AGREGAR LIBRO
        btnAgregarLibro.setOnClickListener {

            val intent = Intent(
                this,
                AgregarLibroActivity::class.java
            )

            startActivity(intent)
        }
        // BOTÓN MI BIBLIOTECA
        val btnBiblioteca =
            findViewById<Button>(R.id.btnBiblioteca)

        btnBiblioteca.setOnClickListener {

            val intent = Intent(
                this,
                BibliotecaActivity::class.java
            )

            startActivity(intent)
        }

        // BOTÓN VER ESTADÍSTICAS
        val btnEstadisticas =
            findViewById<Button>(R.id.btnEstadisticas)

        btnEstadisticas.setOnClickListener {
            val intent = Intent(
                this,
                EstadisticasActivity::class.java
            )
            startActivity(intent)
        }


        // CERRAR SESIÓN
        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            irAlLogin()
        }
    }

    // RECUPERAR META ANUAL
    private fun cargarMeta() {

        database.addValueEventListener(
            object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    metaActual =
                        snapshot.getValue(Int::class.java) ?: 0

                    etMeta.setText(
                        if (metaActual > 0)
                            metaActual.toString()
                        else ""
                    )

                    actualizarResumen()
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(
                        this@DashboardActivity,
                        "Error al cargar meta: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    // RECUPERAR LIBROS DESDE FIREBASE
    private fun cargarLibros() {

        librosRef.addValueEventListener(
            object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    librosTerminados = 0

                    val librosLeyendo = mutableListOf<String>()

                    for (libro in snapshot.children) {

                        val estado = libro.child("estado")
                            .getValue(String::class.java) ?: ""

                        val titulo = libro.child("titulo")
                            .getValue(String::class.java) ?: ""

                        val fecha = libro.child("fechaTerminado")
                            .getValue(String::class.java) ?: ""

                        if (
                            estado == "Terminado" &&
                            fecha.startsWith(anioActual)
                        ) {
                            librosTerminados++
                        }

                        if (estado == "Leyendo") {
                            librosLeyendo.add(titulo)
                        }
                    }

                    tvLeyendo.text =
                        if (librosLeyendo.isEmpty()) {
                            "Todavía no tienes libros en lectura."
                        } else {
                            librosLeyendo.joinToString("\n")
                        }

                    actualizarResumen()
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(
                        this@DashboardActivity,
                        "Error al cargar libros: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    // ACTUALIZAR ESTADÍSTICAS
    private fun actualizarResumen() {

        tvMeta.text =
            "Meta $anioActual: $metaActual libros"

        tvTerminados.text =
            "Libros terminados: $librosTerminados"

        val faltantes =
            (metaActual - librosTerminados).coerceAtLeast(0)

        tvFaltantes.text =
            "Libros faltantes: $faltantes"

        val porcentaje =
            if (metaActual > 0) {
                ((librosTerminados.toDouble() / metaActual) * 100)
                    .toInt()
                    .coerceIn(0, 100)
            } else {
                0
            }

        progresoMeta.progress = porcentaje
    }

    // REGRESAR AL LOGIN
    private fun irAlLogin() {

        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
