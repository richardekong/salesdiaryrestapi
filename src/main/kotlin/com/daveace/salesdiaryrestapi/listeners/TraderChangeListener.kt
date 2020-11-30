package com.daveace.salesdiaryrestapi.listeners

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.domain.Trader
import reactor.core.publisher.Mono

interface TraderChangeListener {
    fun onAddProduct(traderId: String, product: Product): Mono<Product>
    fun onAddCustomer(traderId: String, customer: Customer): Mono<Customer>
    fun <V> onTraderUpdate(traderId: String, trader: MutableMap<String, V>): Mono<Trader>
    fun <V> onUpdateTraderCustomer(traderId: String, customerEmail: String, customer: MutableMap<String, V>): Mono<Customer>
    fun <V> onUpdateTraderProduct(traderId: String, product: MutableMap<String, V>, productId: String): Mono<Product>
    fun onDeleteTraderProduct(traderId: String, productId: String): Mono<Product>
    fun onDeleteTraderCustomer(traderId: String, customerEmail: String): Mono<Customer>
}
