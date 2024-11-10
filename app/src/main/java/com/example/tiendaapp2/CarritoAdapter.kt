package com.example.tiendaapp2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class CarritoAdapter(
    private val context: Context,
    private val carritoItems: MutableList<ProductCartItem>,
    private val updateTotalCallback: () -> Unit
) : RecyclerView.Adapter<CarritoAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       private val nameTextView: TextView = itemView.findViewById(R.id.productName)
       private val priceTextView: TextView = itemView.findViewById(R.id.productPrice)
       private val quantityTextView: TextView = itemView.findViewById(R.id.productQuantity)
       private val addButton: Button = itemView.findViewById(R.id.addButton)
       private val removeButton: Button = itemView.findViewById(R.id.removeButton)
       private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(item: ProductCartItem) {
            nameTextView.text = item.name
            priceTextView.text = context.getString(R.string.product_price, item.price)
            quantityTextView.text = context.getString(R.string.product_quantity, item.quantity)

            addButton.setOnClickListener { updateQuantity(item, item.quantity + 1) }
            removeButton.setOnClickListener { updateQuantity(item, item.quantity - 1) }
            deleteButton.setOnClickListener { removeItem(bindingAdapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(carritoItems[position])
    }

    override fun getItemCount(): Int = carritoItems.size

    private fun updateQuantity(item: ProductCartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            // Remover el ítem y notificar que ha sido removido
            val position = carritoItems.indexOf(item)
            removeItem(position)
        } else {
            // Actualizar la cantidad del ítem
            val position = carritoItems.indexOf(item)
            item.quantity = newQuantity
            notifyItemChanged(position)  // Solo notificar que este ítem ha cambiado
            updateCartInFirestore()
        }
        updateTotalCallback()
    }

    private fun removeItem(position: Int) {
        carritoItems.removeAt(position)  // Eliminar el ítem de la lista
        notifyItemRemoved(position)  // Notificar que un ítem ha sido removido
        updateCartInFirestore()  // Actualizar el carrito en Firestore
        updateTotalCallback()  // Actualizar el total
    }

    private fun updateCartInFirestore() {
        db.collection("carts").document(userId)
            .set(mapOf("products" to carritoItems.map { it.toMap() }))
    }
}
