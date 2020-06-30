package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.domain.Trader
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveTraderService {
    fun save(trader: Trader): Mono<Trader>
    fun saveIfAbsent(trader: Trader): Mono<Trader>
    fun addProduct(traderEmail: String, product: Product): Mono<Product>
    fun addCustomer(traderEmail: String, customer: Customer): Mono<Customer>
    fun findTraderById(id: String): Mono<Trader>
    fun findTrader(email: String): Mono<Trader>
    fun findAllTraders(): Flux<Trader>
    fun findTraderProduct(traderId: String, productId: String): Mono<Product>
    fun findTraderProducts(traderId: String): Flux<Product>
    fun findTraderCustomer(traderId: String, customerEmail: String): Mono<Customer>
    fun findTraderCustomers(traderId: String): Flux<Customer>
    fun <V> updateTrader(traderId:String, trader: MutableMap<String, V>): Mono<Trader>
    fun <V> updateTraderCustomer(traderId: String, customer: MutableMap<String, V>, customerEmail: String): Mono<Customer>
    fun <V> updateTraderProduct(traderId: String, product: MutableMap<String, V>, productId: String): Mono<Product>
    fun deleteTraderProduct(traderId: String, productId: String): Mono<Void>
    fun deleteTraderCustomer(traderId: String, customerEmail: String): Mono<Void>
}