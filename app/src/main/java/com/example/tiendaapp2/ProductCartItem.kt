package com.example.tiendaapp2

data class ProductCartItem(
    val id: String,
    val name: String,
    val price: Double,
    var quantity: Int
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "price" to price,
            "quantity" to quantity
        )
    }
}

