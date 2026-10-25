
package com.example.lecturaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val btnComenzar =
            findViewById<Button>(R.id.btnComenzar)

        btnComenzar.setOnClickListener {

            Toast.makeText(
                this,
                "Botón funcionando",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            startActivity(intent)
        }
    }
}
