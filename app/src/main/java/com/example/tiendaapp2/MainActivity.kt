package com.example.tiendaapp2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()



        // Verificar si el usuario está autenticado
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Si no hay usuario, redirige al login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val btnPerfil = findViewById<Button>(R.id.btnPerfil)
        val btnListaProductos = findViewById<Button>(R.id.btnListaProductos)
        val btnCarritoCompras = findViewById<Button>(R.id.btnCarritoCompras)
        val btnGeolocalizacion = findViewById<Button>(R.id.btnGeolocalizacion)

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnListaProductos.setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
        }

        btnCarritoCompras.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        btnGeolocalizacion.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        // Botón de cerrar sesión
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}