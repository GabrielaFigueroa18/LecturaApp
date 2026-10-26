
package com.example.lecturaapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditarLibroActivity : AppCompatActivity() {

    private lateinit var libroRef: DatabaseReference

    private lateinit var etTitulo: EditText
    private lateinit var etAutor: EditText
    private lateinit var etTotal: EditText
    private lateinit var etLeidas: EditText
    private lateinit var spEstado: Spinner
    private lateinit var rating: RatingBar
    private lateinit var tvCalificacion: TextView
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button

    private val estados = arrayOf(
        "Por leer",
        "Leyendo",
        "Terminado"
    )

    private var fechaAnterior = ""
    private var estadoAnterior = ""
    private var generoAnterior = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_libro)

        val usuario = FirebaseAuth.getInstance().currentUser
        val libroId = intent.getStringExtra("libroId")

        if (usuario == null || libroId.isNullOrBlank()) {
            Toast.makeText(
                this,
                "No se pudo abrir el libro",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val database = FirebaseDatabase.getInstance(
            "https://lecturaapp-78791-default-rtdb.firebaseio.com"
        )

        libroRef = database.getReference("usuarios")
            .child(usuario.uid)
            .child("libros")
            .child(libroId)

        etTitulo = findViewById(R.id.etEditarTitulo)
        etAutor = findViewById(R.id.etEditarAutor)
        etTotal = findViewById(R.id.etEditarTotal)
        etLeidas = findViewById(R.id.etEditarLeidas)
        spEstado = findViewById(R.id.spEditarEstado)
        rating = findViewById(R.id.ratingEditarLibro)
        tvCalificacion = findViewById(R.id.tvEditarCalificacion)
        btnGuardar = findViewById(R.id.btnActualizarLibro)
        btnEliminar = findViewById(R.id.btnEliminarLibro)

        val btnCancelar =
            findViewById<Button>(R.id.btnCancelarEdicion)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            estados
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spEstado.adapter = adapter

        spEstado.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val terminado =
                        estados[position] == "Terminado"

                    rating.visibility =
                        if (terminado) View.VISIBLE else View.GONE

                    tvCalificacion.visibility =
                        if (terminado) View.VISIBLE else View.GONE
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}
            }

        btnGuardar.isEnabled = false
        btnEliminar.isEnabled = false

        // CARGAR DATOS DEL LIBRO
        libroRef.get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) {
                    Toast.makeText(
                        this,
                        "El libro ya no existe",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@addOnSuccessListener
                }

                etTitulo.setText(
                    snapshot.child("titulo")
                        .getValue(String::class.java) ?: ""
                )

                etAutor.setText(
                    snapshot.child("autor")
                        .getValue(String::class.java) ?: ""
                )

                val total = snapshot.child("totalPaginas")
                    .getValue(Int::class.java) ?: 0

                val leidas = snapshot.child("paginasLeidas")
                    .getValue(Int::class.java) ?: 0

                etTotal.setText(total.toString())
                etLeidas.setText(leidas.toString())

                estadoAnterior = snapshot.child("estado")
                    .getValue(String::class.java) ?: "Por leer"

                generoAnterior = snapshot.child("genero")
                    .getValue(String::class.java) ?: ""

                fechaAnterior = snapshot.child("fechaTerminado")
                    .getValue(String::class.java) ?: ""

                val posicion = estados.indexOf(estadoAnterior)
                spEstado.setSelection(
                    if (posicion >= 0) posicion else 0
                )

                rating.rating = (
                        snapshot.child("calificacion")
                            .getValue(Int::class.java) ?: 0
                        ).toFloat()

                btnGuardar.isEnabled = true
                btnEliminar.isEnabled = true
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Error al cargar: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        // GUARDAR CAMBIOS
        btnGuardar.setOnClickListener {

            val titulo = etTitulo.text.toString().trim()
            val autor = etAutor.text.toString().trim()
            val total = etTotal.text.toString().toIntOrNull()
            val leidas = etLeidas.text.toString().toIntOrNull()
            val estado = spEstado.selectedItem.toString()
            val calificacion = rating.rating.toInt()

            if (titulo.length !in 2..60) {
                etTitulo.error =
                    "El título debe tener entre 2 y 60 caracteres"
                return@setOnClickListener
            }

            if (autor.length !in 2..60) {
                etAutor.error =
                    "El autor debe tener entre 2 y 60 caracteres"
                return@setOnClickListener
            }

            if (total == null || total <= 0) {
                etTotal.error = "Ingresa un total válido"
                return@setOnClickListener
            }

            if (leidas == null || leidas < 0 || leidas > total) {
                etLeidas.error =
                    "Las páginas leídas deben estar entre 0 y $total"
                return@setOnClickListener
            }

            if (estado == "Terminado" && calificacion !in 1..5) {
                Toast.makeText(
                    this,
                    "Califica el libro de 1 a 5 estrellas",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val fechaTerminado =
                if (estado == "Terminado") {
                    if (
                        estadoAnterior == "Terminado" &&
                        fechaAnterior.isNotBlank()
                    ) {
                        fechaAnterior
                    } else {
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.US
                        ).format(Date())
                    }
                } else {
                    ""
                }

            val cambios = mapOf<String, Any>(
                "titulo" to titulo,
                "autor" to autor,
                "genero" to generoAnterior,
                "totalPaginas" to total,
                "paginasLeidas" to leidas,
                "estado" to estado,
                "calificacion" to
                        (if (estado == "Terminado") calificacion else 0),
                "fechaTerminado" to fechaTerminado
            )

            btnGuardar.isEnabled = false

            libroRef.updateChildren(cambios)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Libro actualizado correctamente",
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

        // ELIMINAR LIBRO
        btnEliminar.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Eliminar libro")
                .setMessage(
                    "¿Estás seguro de eliminar este libro? " +
                            "Esta acción no se puede deshacer."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar") { _, _ ->

                    btnEliminar.isEnabled = false

                    libroRef.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Libro eliminado",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            btnEliminar.isEnabled = true
                            Toast.makeText(
                                this,
                                "Error: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .show()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}
