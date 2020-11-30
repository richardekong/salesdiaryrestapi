package com.daveace.salesdiaryrestapi.listeners

import com.daveace.salesdiaryrestapi.domain.Product

interface StockChangeListener {

    fun onStockChange(product: Product)
}