
package com.example.lecturaapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgregarLibroActivity : AppCompatActivity() {

    private val generos = arrayOf(
        "Novela",
        "Ciencia ficción",
        "Historia",
        "Desarrollo personal"
    )

    private val estados = arrayOf(
        "Por leer",
        "Leyendo",
        "Terminado"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_libro)

        val etTitulo = findViewById<EditText>(R.id.etTitulo)
        val etAutor = findViewById<EditText>(R.id.etAutor)
        val etTotal = findViewById<EditText>(R.id.etTotalPaginas)
        val etLeidas = findViewById<EditText>(R.id.etPaginasLeidas)

        val spGenero = findViewById<Spinner>(R.id.spGenero)
        val spEstado = findViewById<Spinner>(R.id.spEstado)

        val rating = findViewById<RatingBar>(R.id.ratingLibro)
        val tvCalificacion =
            findViewById<TextView>(R.id.tvCalificacion)

        val btnGuardar =
            findViewById<Button>(R.id.btnGuardarLibro)

        val adaptadorGenero = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            generos
        )

        adaptadorGenero.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spGenero.adapter = adaptadorGenero

        val adaptadorEstado = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            estados
        )

        adaptadorEstado.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spEstado.adapter = adaptadorEstado

        // La calificación solo se muestra
        // cuando el estado es Terminado.
        rating.visibility = View.GONE
        tvCalificacion.visibility = View.GONE

        spEstado.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val terminado = estados[position] == "Terminado"

                    rating.visibility =
                        if (terminado) View.VISIBLE else View.GONE

                    tvCalificacion.visibility =
                        if (terminado) View.VISIBLE else View.GONE

                    if (!terminado) rating.rating = 0f
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }

        btnGuardar.setOnClickListener {

            val titulo = etTitulo.text.toString().trim()
            val autor = etAutor.text.toString().trim()
            val total = etTotal.text.toString().toIntOrNull()
            val leidas = etLeidas.text.toString().toIntOrNull()

            val genero = spGenero.selectedItem.toString()
            val estado = spEstado.selectedItem.toString()
            val calificacion = rating.rating.toInt()

            if (titulo.length !in 2..60) {
                etTitulo.error = "El título debe tener entre 2 y 60 caracteres"
                return@setOnClickListener
            }

            if (autor.length !in 2..60) {
                etAutor.error = "El autor debe tener entre 2 y 60 caracteres"
                return@setOnClickListener
            }

            if (total == null || total <= 0) {
                etTotal.error = "Ingresa un total de páginas mayor que cero"
                return@setOnClickListener
            }

            if (leidas == null || leidas < 0 || leidas > total) {
                etLeidas.error = "Las páginas leídas deben estar entre 0 y $total"
                return@setOnClickListener
            }

            if (estado == "Terminado" && calificacion !in 1..5) {
                Toast.makeText(
                    this,
                    "Debes calificar el libro terminado de 1 a 5 estrellas",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val usuario = FirebaseAuth.getInstance().currentUser

            if (usuario == null) {
                Toast.makeText(
                    this,
                    "Debes iniciar sesión",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance(
                "https://lecturaapp-78791-default-rtdb.firebaseio.com"
            )

            val referencia = database.getReference("usuarios")
                .child(usuario.uid)
                .child("libros")
                .push()

            val fechaTerminado =
                if (estado == "Terminado") {
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                    ).format(Date())
                } else {
                    ""
                }

            val libro = hashMapOf<String, Any>(
                "id" to (referencia.key ?: ""),
                "titulo" to titulo,
                "autor" to autor,
                "genero" to genero,
                "totalPaginas" to total,
                "paginasLeidas" to leidas,
                "estado" to estado,
                "calificacion" to
                        (if (estado == "Terminado") calificacion else 0),
                "fechaTerminado" to fechaTerminado
            )

            btnGuardar.isEnabled = false

            referencia.setValue(libro)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Libro guardado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
                .addOnFailureListener { error ->

                    btnGuardar.isEnabled = true

                    Toast.makeText(
                        this,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
