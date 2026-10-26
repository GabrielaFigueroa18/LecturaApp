
package com.example.lecturaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

class EstadisticasActivity : AppCompatActivity() {

    private var meta = 0
    private var total = 0
    private var terminadosAnio = 0
    private var terminadosTodos = 0
    private var leyendo = 0
    private var porLeer = 0

    private val anio =
        Calendar.getInstance().get(Calendar.YEAR).toString()

    private lateinit var usuarioRef: DatabaseReference
    private val listeners = mutableListOf<Pair<DatabaseReference, ValueEventListener>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        findViewById<TextView>(R.id.tvAnioEstadisticas).text =
            "Resumen de tus hábitos de lectura · $anio"

        findViewById<Button>(R.id.btnVolverEst)
            .setOnClickListener { finish() }

        usuarioRef = FirebaseDatabase.getInstance(
            "https://lecturaapp-78791-default-rtdb.firebaseio.com"
        ).getReference("usuarios").child(usuario.uid)

        val metaRef = usuarioRef.child("metas").child(anio)

        val metaListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                meta = snapshot.getValue(Int::class.java) ?: 0
                actualizarPantalla()
            }

            override fun onCancelled(error: DatabaseError) {
                mostrarError(error)
            }
        }

        metaRef.addValueEventListener(metaListener)
        listeners.add(metaRef to metaListener)

        val librosRef = usuarioRef.child("libros")

        val librosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                total = 0
                terminadosAnio = 0
                terminadosTodos = 0
                leyendo = 0
                porLeer = 0

                for (libro in snapshot.children) {
                    total++

                    val estado = libro.child("estado")
                        .getValue(String::class.java) ?: ""

                    val fecha = libro.child("fechaTerminado")
                        .getValue(String::class.java) ?: ""

                    when (estado) {
                        "Terminado" -> {
                            terminadosTodos++
                            if (fecha.startsWith(anio)) {
                                terminadosAnio++
                            }
                        }
                        "Leyendo" -> leyendo++
                        "Por leer" -> porLeer++
                    }
                }

                actualizarPantalla()
            }

            override fun onCancelled(error: DatabaseError) {
                mostrarError(error)
            }
        }

        librosRef.addValueEventListener(librosListener)
        listeners.add(librosRef to librosListener)
    }

    private fun actualizarPantalla() {

        findViewById<TextView>(R.id.tvTotalEst).text =
            "Total de libros: $total"

        findViewById<TextView>(R.id.tvTerminadosEst).text =
            "Terminados este año: $terminadosAnio"

        findViewById<TextView>(R.id.tvLeyendoEst).text =
            "Leyendo: $leyendo"

        findViewById<TextView>(R.id.tvPorLeerEst).text =
            "Por leer: $porLeer"

        findViewById<TextView>(R.id.tvMetaEst).text =
            "$terminadosAnio de $meta libros"

        val porcentajeMeta =
            if (meta > 0) {
                ((terminadosAnio.toDouble() / meta) * 100)
                    .toInt().coerceIn(0, 100)
            } else 0

        findViewById<ProgressBar>(R.id.progresoEst).progress =
            porcentajeMeta

        findViewById<TextView>(R.id.tvPorcentajeEst).text =
            "$porcentajeMeta% completado"

        findViewById<TextView>(R.id.tvDistribucionEst).text =
            "Terminados: $terminadosTodos\n" +
                    "Leyendo: $leyendo\n" +
                    "Por leer: $porLeer"

        fun porcentaje(cantidad: Int): Int =
            if (total > 0) {
                ((cantidad.toDouble() / total) * 100)
                    .toInt().coerceIn(0, 100)
            } else 0

        findViewById<ProgressBar>(R.id.barTerminados).progress =
            porcentaje(terminadosTodos)

        findViewById<ProgressBar>(R.id.barLeyendo).progress =
            porcentaje(leyendo)

        findViewById<ProgressBar>(R.id.barPorLeer).progress =
            porcentaje(porLeer)
    }

    private fun mostrarError(error: DatabaseError) {
        Toast.makeText(
            this,
            "Error al cargar estadísticas: ${error.message}",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        listeners.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        listeners.clear()
        super.onDestroy()
    }
}
