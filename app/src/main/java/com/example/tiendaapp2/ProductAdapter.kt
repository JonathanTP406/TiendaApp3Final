package com.example.tiendaapp2 // Asegúrate de que el nombre del paquete coincida

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductAdapter(private val context: Context, private val products: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)

        fun bind(product: Product) {
            productName.text = product.name
            productPrice.text = product.price.toString()

            addToCartButton.setOnClickListener {
                addToCart(product)
            }
        }
    }

    // Función para agregar al carrito
    private fun addToCart(product: Product) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Verificar si el carrito ya existe
        val cartRef = db.collection("carts").document(userId)

        // Empezamos con un carrito vacío
        cartRef.get().addOnSuccessListener { document ->
            val cart = document.toObject(Cart::class.java) ?: Cart()

            // Agregar el producto al carrito
            cart.products.add(ProductCartItem(product.id, product.name, product.price, 1))

            // Actualizar el carrito en Firebase
            cartRef.set(cart)
                .addOnSuccessListener {
                    Toast.makeText(context, "${product.name} añadido al carrito", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error al agregar al carrito: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}






