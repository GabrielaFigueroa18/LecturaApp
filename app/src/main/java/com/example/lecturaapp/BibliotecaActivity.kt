
package com.example.lecturaapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BibliotecaActivity : AppCompatActivity() {

    private lateinit var contenedor: LinearLayout
    private lateinit var tvCantidad: TextView
    private lateinit var tvResultados: TextView

    private val libros = mutableListOf<LibroBiblioteca>()
    private var filtroActual = "Todos"
    private var busqueda = ""

    private val morado = Color.parseColor("#7135E8")
    private val gris = Color.parseColor("#64748B")
    private val oscuro = Color.parseColor("#25115E")

    data class LibroBiblioteca(
        val id: String,
        val titulo: String,
        val autor: String,
        val genero: String,
        val estado: String,
        val total: Int,
        val leidas: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioteca)

        contenedor = findViewById(R.id.contenedorLibros)
        tvCantidad = findViewById(R.id.tvCantidadLibros)
        tvResultados = findViewById(R.id.tvResultados)

        val buscar = findViewById<EditText>(R.id.etBuscarLibro)

        val btnTodos = findViewById<Button>(R.id.btnTodos)
        val btnPorLeer = findViewById<Button>(R.id.btnPorLeer)
        val btnLeyendo = findViewById<Button>(R.id.btnLeyendo)
        val btnTerminados = findViewById<Button>(R.id.btnTerminados)

        val botones = listOf(
            btnTodos,
            btnPorLeer,
            btnLeyendo,
            btnTerminados
        )

        fun seleccionarFiltro(filtro: String, seleccionado: Button) {
            filtroActual = filtro

            botones.forEach { boton ->
                boton.backgroundTintList =
                    ColorStateList.valueOf(
                        if (boton == seleccionado) morado else gris
                    )
            }

            mostrarLibros()
        }

        btnTodos.setOnClickListener {
            seleccionarFiltro("Todos", btnTodos)
        }

        btnPorLeer.setOnClickListener {
            seleccionarFiltro("Por leer", btnPorLeer)
        }

        btnLeyendo.setOnClickListener {
            seleccionarFiltro("Leyendo", btnLeyendo)
        }

        btnTerminados.setOnClickListener {
            seleccionarFiltro("Terminado", btnTerminados)
        }

        buscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int,
                before: Int, count: Int
            ) {
                busqueda = s.toString().trim()
                mostrarLibros()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            finish()
        }

        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val librosRef = FirebaseDatabase.getInstance(
            "https://lecturaapp-78791-default-rtdb.firebaseio.com"
        ).getReference("usuarios")
            .child(usuario.uid)
            .child("libros")

        librosRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                libros.clear()

                for (item in snapshot.children) {
                    val id = item.key ?: continue

                    libros.add(
                        LibroBiblioteca(
                            id = id,
                            titulo = item.child("titulo")
                                .getValue(String::class.java) ?: "",
                            autor = item.child("autor")
                                .getValue(String::class.java) ?: "",
                            genero = item.child("genero")
                                .getValue(String::class.java) ?: "",
                            estado = item.child("estado")
                                .getValue(String::class.java) ?: "Por leer",
                            total = item.child("totalPaginas")
                                .getValue(Int::class.java) ?: 0,
                            leidas = item.child("paginasLeidas")
                                .getValue(Int::class.java) ?: 0
                        )
                    )
                }

                mostrarLibros()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@BibliotecaActivity,
                    "Error al cargar libros: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun mostrarLibros() {
        contenedor.removeAllViews()

        tvCantidad.text = "📚 Total de libros: ${libros.size}"

        val filtrados = libros.filter { libro ->

            val coincideBusqueda =
                libro.titulo.contains(busqueda, ignoreCase = true) ||
                        libro.autor.contains(busqueda, ignoreCase = true)

            val coincideEstado =
                filtroActual == "Todos" ||
                        libro.estado == filtroActual

            coincideBusqueda && coincideEstado
        }

        tvResultados.text =
            "${filtrados.size} libro(s) encontrados"

        if (filtrados.isEmpty()) {
            val vacio = TextView(this).apply {
                text = "📖 No se encontraron libros.\nPrueba otra búsqueda o filtro."
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(gris)
                setPadding(16, 50, 16, 50)
            }

            contenedor.addView(vacio)
            return
        }

        filtrados.forEach { libro ->

            val porcentaje =
                if (libro.total > 0) {
                    ((libro.leidas.toDouble() / libro.total) * 100)
                        .toInt().coerceIn(0, 100)
                } else 0

            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 20, 20, 20)
                setBackgroundResource(R.drawable.bg_tarjeta)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 18
                }
            }

            val titulo = TextView(this).apply {
                text = "📖 ${libro.titulo}"
                textSize = 20f
                setTextColor(oscuro)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val autor = TextView(this).apply {
                text = "Autor: ${libro.autor}"
                textSize = 15f
                setTextColor(gris)
                setPadding(0, 12, 0, 4)
            }

            val genero = TextView(this).apply {
                text = "Género: ${libro.genero}"
                textSize = 14f
                setTextColor(gris)
            }

            val estado = TextView(this).apply {
                text = "● ${libro.estado}"
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(
                    when (libro.estado) {
                        "Terminado" -> Color.parseColor("#15803D")
                        "Leyendo" -> Color.parseColor("#2563EB")
                        else -> morado
                    }
                )
                setPadding(0, 12, 0, 12)
            }

            val detalle = TextView(this).apply {
                text = "${libro.leidas} de ${libro.total} páginas · $porcentaje%"
                textSize = 14f
                setTextColor(oscuro)
            }

            val progreso = ProgressBar(
                this, null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                max = 100
                progress = porcentaje
                progressTintList = ColorStateList.valueOf(morado)
                progressBackgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#EDE9F3")
                    )

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    14
                ).apply {
                    topMargin = 12
                    bottomMargin = 16
                }
            }

            val editar = Button(this).apply {
                text = "✏ Editar libro"
                isAllCaps = false
                setTextColor(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(morado)

                setOnClickListener {
                    val intent = Intent(
                        this@BibliotecaActivity,
                        EditarLibroActivity::class.java
                    )
                    intent.putExtra("libroId", libro.id)
                    startActivity(intent)
                }
            }

            tarjeta.addView(titulo)
            tarjeta.addView(autor)
            tarjeta.addView(genero)
            tarjeta.addView(estado)
            tarjeta.addView(detalle)
            tarjeta.addView(progreso)
            tarjeta.addView(editar)

            contenedor.addView(tarjeta)
        }
    }
}
