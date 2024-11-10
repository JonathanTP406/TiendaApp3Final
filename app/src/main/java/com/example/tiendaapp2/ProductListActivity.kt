package com.example.tiendaapp2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


class ProductListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>() // Aquí obtendrás los productos de Firebase o una fuente local

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar productos de Firebase
        loadProductsFromFirestore()

        productAdapter = ProductAdapter(this, products)
        recyclerView.adapter = productAdapter
    }

    private fun loadProductsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    products.add(product)
                }
                productAdapter.notifyDataSetChanged()
                // Notificar que los productos han sido cargados
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar productos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}




