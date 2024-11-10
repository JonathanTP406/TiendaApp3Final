package com.example.tiendaapp2

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CarritoActivity : AppCompatActivity() {

    private lateinit var recyclerViewCarrito: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var carritoAdapter: CarritoAdapter
    private lateinit var db: FirebaseFirestore
    private val carritoItems = mutableListOf<ProductCartItem>()
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        db = FirebaseFirestore.getInstance()
        recyclerViewCarrito = findViewById(R.id.recyclerViewCarrito)
        totalTextView = findViewById(R.id.totalTextView)

        setupRecyclerView()
        loadCartItems()
    }

    private fun setupRecyclerView() {
        carritoAdapter = CarritoAdapter(this, carritoItems) { updateTotal() }
        recyclerViewCarrito.layoutManager = LinearLayoutManager(this)
        recyclerViewCarrito.adapter = carritoAdapter
    }

    private fun loadCartItems() {
        db.collection("carts").document(userId).get().addOnSuccessListener { document ->
            document?.get("products")?.let { products ->
                val newItems = mutableListOf<ProductCartItem>()
                if (products is List<*>) {
                    for (product in products.filterIsInstance<Map<String, Any>>()) {
                        val id = product["id"] as? String ?: "" // Obtener el id
                        newItems.add(ProductCartItem(
                            id = id, // Pasar el id
                            name = product["name"] as? String ?: "",
                            price = (product["price"] as? Number)?.toDouble() ?: 0.0,
                            quantity = (product["quantity"] as? Number)?.toInt() ?: 0
                        ))
                    }
                }

                // Comparar la lista existente con la nueva y notificar los cambios específicos
                for (i in newItems.indices) {
                    if (i < carritoItems.size && carritoItems[i] != newItems[i]) {
                        carritoItems[i] = newItems[i]
                        carritoAdapter.notifyItemChanged(i)
                    } else if (i >= carritoItems.size) {
                        carritoItems.add(newItems[i])
                        carritoAdapter.notifyItemInserted(i)
                    }
                }

                // Si la nueva lista es más corta, elimina los elementos sobrantes
                if (newItems.size < carritoItems.size) {
                    val removeCount = carritoItems.size - newItems.size
                    for (i in 1..removeCount) {
                        carritoItems.removeAt(carritoItems.lastIndex)
                        carritoAdapter.notifyItemRemoved(carritoItems.size)
                    }
                }

                updateTotal()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar el carrito", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateTotal() {
        val total = carritoItems.sumOf { it.price * it.quantity }
        totalTextView.text = getString(R.string.total_text, total)
    }
}
