package com.example.tiendaapp2

data class Cart(
    val products: MutableList<ProductCartItem> = mutableListOf()
)