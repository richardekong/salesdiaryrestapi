package com.daveace.salesdiaryrestapi.listeners

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.service.ReactiveCustomerService
import com.daveace.salesdiaryrestapi.service.ReactiveProductService
import com.daveace.salesdiaryrestapi.service.ReactiveTraderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class TraderChangeListenerImpl : TraderChangeListener {

    private lateinit var traderService: ReactiveTraderService
    private lateinit var productService: ReactiveProductService
    private lateinit var customerService: ReactiveCustomerService

    companion object {
        private const val NAME = "name"
        private const val PHONE = "phone"
        private const val ADDRESS = "address"
        private const val LOCATION = "location"
        private const val SIGNATURE_PATH = "signaturePath"
        private const val COMPANY = "company"
        private const val STOCK = "stock"
        private const val COST = "cost"
        private const val CODE = "code"
        private const val IMAGE_PATH = "imagePath"
    }

    @Autowired
    fun initTraderService(traderService: ReactiveTraderService) {
        this.traderService = traderService
    }

    @Autowired
    fun initProductService(productService: ReactiveProductService) {
        this.productService = productService
    }

    @Autowired
    fun initCustomerService(customerService: ReactiveCustomerService) {
        this.customerService = customerService
    }

    override fun onAddProduct(traderId: String, product: Product): Mono<Product> {
        return productService.existsByName(product.name).filter {
            if (it) throw RuntimeException("${product.name} exists")
            else it.not()
        }.flatMap {
            traderService.findTraderById(traderId).flatMap { trader ->
                product.traderId = trader.id
                trader.products.add(product)
                traderService.save(trader).apply { subscribe() }
                Mono.just(product)
            }
        }
    }

    override fun onAddCustomer(traderId: String, customer: Customer): Mono<Customer> {
        return customerService.existsByEmail(customer.email)
                .filter {
                    if (it) throw RuntimeException("Customer with ${customer.email} exists")
                    else it.not()
                }
                .flatMap {
                    traderService.findTraderById(traderId).flatMap { trader ->
                        customer.traderId = trader.id
                        trader.customers.add(customer)
                        traderService.save(trader).apply { subscribe() }
                        Mono.just(customer)
                    }
                }
    }

    override fun <V> onTraderUpdate(traderId: String, trader: MutableMap<String, V>): Mono<Trader> {
        return traderService.findTraderById(traderId)
                .switchIfEmpty(Mono.fromRunnable { throw RuntimeException(HttpStatus.UNAUTHORIZED.reasonPhrase) })
                .flatMap { storedTrader ->
                    if (trader.containsKey(NAME) && (trader[NAME] as String).isNotEmpty())
                        storedTrader.name = trader[NAME] as String
                    if (trader.containsKey(PHONE) && (trader[PHONE] as String).isNotEmpty())
                        storedTrader.phone = trader[PHONE] as String
                    if (trader.containsKey(ADDRESS) && (trader[ADDRESS] as String).isNotEmpty())
                        storedTrader.address = trader[ADDRESS] as String
                    if (trader.containsKey(LOCATION) && isLocationValidFor(trader)) {
                        val location: MutableList<*> = trader[LOCATION] as MutableList<*>
                        storedTrader.location = mutableListOf()
                        location.asSequence().forEach { value ->
                            storedTrader.location.add(value as Double)
                        }
                    }
                    Mono.just(storedTrader)
                }
    }

    override fun <V> onUpdateTraderCustomer(traderId: String, customerEmail: String, customer: MutableMap<String, V>): Mono<Customer> {
        return traderService.findTraderById(traderId)
                .switchIfEmpty(Mono.fromRunnable { throw RuntimeException(HttpStatus.UNAUTHORIZED.reasonPhrase) })
                .flatMap { trader ->
                    val customerToUpdate: Customer = trader.customers.asSequence().find { it.email == customerEmail }!!
                    val indexToUpdate: Int = trader.customers.indexOf(customerToUpdate)

                    if (customer.containsKey(NAME) && (customer[NAME] as String).isNotEmpty())
                        customerToUpdate.name = customer[NAME] as String
                    if (customer.containsKey(PHONE) && (customer[PHONE] as String).isNotEmpty())
                        customerToUpdate.phone = customer[PHONE] as String
                    if (customer.containsKey(ADDRESS) && (customer[ADDRESS] as String).isNotEmpty())
                        customerToUpdate.address = customer[ADDRESS] as String
                    if (customer.containsKey(COMPANY) && (customer[COMPANY] as String).isNotEmpty())
                        customerToUpdate.company = customer[COMPANY] as String
                    if (customer.containsKey(SIGNATURE_PATH) && (customer[SIGNATURE_PATH] as String).isNotEmpty())
                        customerToUpdate.signaturePath = customer[SIGNATURE_PATH] as String
                    if (customer.containsKey(LOCATION) && isLocationValidFor(customer)) {
                        customerToUpdate.location = mutableListOf()
                        val location: MutableList<*> = customer[LOCATION] as MutableList<*>
                        location.asSequence().forEach { value ->
                            customerToUpdate.location.add(value as Double)
                        }
                    }
                    trader.customers[indexToUpdate] = customerToUpdate
                    val traderUpdateOps: Mono<Trader> = traderService.save(trader).apply { subscribe() }
                    traderUpdateOps.flatMap { Mono.just(it.customers[indexToUpdate]) }
                }
    }

    override fun <T> onUpdateTraderProduct(traderId: String, product: MutableMap<String, T>, productId: String): Mono<Product> {
        return traderService.findTraderById(traderId)
                .switchIfEmpty(Mono.fromRunnable { throw RuntimeException(HttpStatus.UNAUTHORIZED.reasonPhrase) })
                .flatMap { trader ->
                    val productToUpdate: Product = trader.products.asSequence().find { it.id == productId }!!
                    val indexToUpdate: Int = trader.products.indexOf(productToUpdate)

                    if (product.containsKey(NAME) && (product[NAME] as String).isNotEmpty())
                        productToUpdate.name = product[NAME] as String
                    if (product.containsKey(COST) && (product[COST] as Double).isNaN().not())
                        productToUpdate.cost = product[COST] as Double
                    if (product.containsKey(STOCK) && (product[STOCK] as Double).isNaN().not())
                        productToUpdate.stock = product[STOCK] as Double
                    if (product.containsKey(IMAGE_PATH) && (product[IMAGE_PATH] as String).isNotEmpty())
                        productToUpdate.imagePath = product[IMAGE_PATH] as String
                    if (product.containsKey(CODE) && (product[CODE] as String).isNotEmpty())
                        productToUpdate.code = product[CODE] as String

                    trader.products[indexToUpdate] = productToUpdate
                    val traderUpdateOps: Mono<Trader> = traderService.save(trader).apply { subscribe() }
                    traderUpdateOps.flatMap { Mono.just(it.products[indexToUpdate]) }
                }
    }

    override fun onDeleteTraderProduct(traderId: String, productId: String): Mono<Product> {
        return traderService.findTraderById(traderId)
                .switchIfEmpty(Mono.fromRunnable { throw RuntimeException(HttpStatus.UNAUTHORIZED.reasonPhrase) })
                .flatMap { trader ->
                    val products: MutableList<Product> = trader.products
                    val removedProduct: Product = products.removeAt(products.asSequence()
                            .indexOfFirst { it.id == productId })
                    traderService.save(trader).apply { subscribe() }
                    Mono.just(removedProduct)
                }
    }

    override fun onDeleteTraderCustomer(traderId: String, customerEmail: String): Mono<Customer> {
        return traderService.findTraderById(traderId).switchIfEmpty(Mono.fromRunnable {
            throw RuntimeException(HttpStatus.UNAUTHORIZED.reasonPhrase)
        }).flatMap { trader ->
            val customers: MutableList<Customer> = trader.customers
            val removedCustomer: Customer = customers.removeAt(customers
                    .asSequence().indexOfFirst { it.email == customerEmail })
            traderService.save(trader).apply { subscribe() }
            Mono.just(removedCustomer)
        }
    }

    private fun <T : Any> isLocationValidFor(entity: T): Boolean {
        return when (entity) {
            is Trader -> entity.location.size == 2
                    && entity.location.asSequence().none { it.isNaN() }
            is Customer -> entity.location.size == 2
                    && entity.location.asSequence().none { it.isNaN() }
            is MutableMap<*, *> -> {
                val location: MutableList<*> = entity[LOCATION] as MutableList<*>
                entity.containsKey(LOCATION) && location.size == 2
                        && location.asSequence().none { (it as Double).isNaN() }
            }
            else -> false
        }
    }
}
