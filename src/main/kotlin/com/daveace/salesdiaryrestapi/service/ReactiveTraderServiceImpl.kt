package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.listeners.TraderChangeListener
import com.daveace.salesdiaryrestapi.repository.ReactiveCustomerRepository
import com.daveace.salesdiaryrestapi.repository.ReactiveProductRepository
import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class ReactiveTraderServiceImpl : ReactiveTraderService {

    private lateinit var traderRepo: ReactiveTraderRepository
    private lateinit var productRepo: ReactiveProductRepository
    private lateinit var productService: ReactiveProductService
    private lateinit var customerService: ReactiveCustomerService
    private lateinit var customerRepo: ReactiveCustomerRepository
    private lateinit var authenticatedUser: AuthenticatedUser
    private lateinit var traderChangeListener: TraderChangeListener
    private lateinit var mailService: MailService

    @Value("\${mailgun.api.email}")
    private lateinit var appEmail: String

    @Autowired
    fun initTraderRepo(traderRepo: ReactiveTraderRepository) {
        this.traderRepo = traderRepo
    }

    @Autowired
    fun initProductRepo(productRepo: ReactiveProductRepository) {
        this.productRepo = productRepo
    }

    @Autowired
    fun initCustomerRepo(customerRepo: ReactiveCustomerRepository) {
        this.customerRepo = customerRepo
    }

    @Autowired
    fun initProductService(productService: ReactiveProductService) {
        this.productService = productService
    }

    @Autowired
    fun initCustomerService(customerService: ReactiveCustomerService) {
        this.customerService = customerService
    }

    @Autowired
    fun initAuthenticatedUser(authenticatedUser: AuthenticatedUser) {
        this.authenticatedUser = authenticatedUser
    }

    @Autowired
    fun initTraderChangeListener(traderChangeListener: TraderChangeListener) {
        this.traderChangeListener = traderChangeListener
    }

    @Autowired
    fun initMailService(mailService: MailService) {
        this.mailService = mailService
    }

    override fun save(trader: Trader): Mono<Trader> {
        return traderRepo.save(trader)
    }

    override fun saveIfAbsent(trader: Trader): Mono<Trader> {
        val email: String = trader.email
        return traderRepo.existsTraderByEmail(email)
                .subscribeOn(Schedulers.parallel())
                .filter { traderExists ->
                    if (traderExists) throw RestException(
                            "Trader with $email exists!")
                    else traderExists.not()
                }.flatMap { traderRepo.save(trader) }
    }

    override fun findTraderById(id: String): Mono<Trader> {
        return traderRepo.findById(id)
    }

    override fun findTrader(email: String): Mono<Trader> {
        return traderRepo.findTraderByEmail(email)
    }

    override fun findAllTraders(): Flux<Trader> {
        return traderRepo.findAll()
    }

    override fun addProduct(traderEmail: String, product: Product): Mono<Product> {
        return authenticatedUser.ownsThisAccount(traderEmail)
                .flatMap {
                    traderChangeListener.onAddProduct(it.id, product)
                }
                .flatMap { productService.saveIfAbsent(it) }
                .doOnSuccess {
                    notifyByEmail(Mail(appEmail, traderEmail, "New Product Addition",
                            "You have just added ${it.name} to your collection."))
                }

    }

    override fun addCustomer(traderEmail: String, customer: Customer): Mono<Customer> {
        return authenticatedUser.ownsThisAccount(traderEmail)
                .flatMap {
                    traderChangeListener.onAddCustomer(it.id, customer)
                }
                .flatMap { customerService.saveIfAbsent(customer) }
                .doOnSuccess {
                    notifyByEmail(Mail(appEmail, traderEmail, "New Customer Addition",
                            "You have just registered ${it.name}."))
                }

    }

    override fun findTraderProduct(traderId: String, productId: String): Mono<Product> {
        return authenticatedUser.getCurrentUser()
                .filter { traderId == it.id }
                .switchIfEmpty(Mono.fromRunnable {
                    throw unAuthorizedAccess()
                }).flatMap { currentUser ->
                    traderRepo.findById(currentUser.id)
                            .flatMap { trader ->
                                Mono.just(trader.products.asSequence()
                                        .filter { it.id == productId }
                                        .first())
                            }.switchIfEmpty(Mono.fromRunnable {
                                throw unAuthorizedAccess()
                            })
                }
    }

    override fun findTraderProducts(traderId: String): Flux<Product> {
        return authenticatedUser.getCurrentUser()
                .filter { traderId == it.id }
                .switchIfEmpty(Mono.fromRunnable {
                    throw unAuthorizedAccess()
                }).flatMapMany {
                    productRepo.findAll().filter { it.traderId == traderId }
                }
    }

    override fun findTraderCustomer(traderId: String, customerEmail: String): Mono<Customer> {
        return authenticatedUser.getCurrentUser()
                .filter { it.id == traderId }
                .switchIfEmpty(Mono.fromRunnable {
                    throw unAuthorizedAccess()
                }).flatMap {
                    customerRepo.findCustomerByEmail(customerEmail)
                            .filter { it.traderId == traderId }
                            .switchIfEmpty(Mono.fromRunnable {
                                throw unAuthorizedAccess()
                            })
                }
    }

    override fun findTraderCustomers(traderId: String): Flux<Customer> {
        return authenticatedUser.getCurrentUser()
                .flatMapMany { currentUser ->
                    customerRepo.findAll().filter { customer ->
                        (currentUser.id == traderId) && (traderId == customer.traderId)
                    }.switchIfEmpty(Mono.fromRunnable {
                        throw unAuthorizedAccess()
                    })
                }
    }

    override fun <V> updateTrader(trader: MutableMap<String, V>): Mono<Trader> {
        return authenticatedUser.getCurrentUser()
                .flatMap { currentUser ->
                    traderChangeListener.onTraderUpdate(currentUser.id, trader)
                }
                .flatMap { storedTrader -> save(storedTrader) }
                .doOnSuccess {
                    notifyByEmail(Mail(appEmail, it.email, "Trader Account Update",
                            "Dear ${it.name}, You have successfully updated your details."))
                }
    }

    override fun <V> updateTraderCustomer(traderId: String, customer: MutableMap<String, V>, customerEmail: String): Mono<Customer> {
        val trader: Trader = findTraderById(traderId).toFuture().join()
        return authenticatedUser.getCurrentUser()
                .filter { it.id == traderId }
                .switchIfEmpty(Mono.fromRunnable { throw unAuthorizedAccess() })
                .flatMap { traderChangeListener.onUpdateTraderCustomer(traderId, customerEmail, customer) }
                .flatMap { storedCustomer -> customerRepo.save(storedCustomer) }
                .doOnSuccess {
                    notifyByEmail(Mail(appEmail, trader.email, "Customer Update",
                            "Dear ${trader.name}, you have successfully updated ${it.name}'s record."))
                }
    }

    override fun <V> updateTraderProduct(traderId: String, product: MutableMap<String, V>, productId: String): Mono<Product> {
        val trader: Trader = findTraderById(traderId).toFuture().join()
        return authenticatedUser.ownsThisAccountById(traderId)
                .map { ownsAccount -> if (ownsAccount.not()) throw unAuthorizedAccess() }
                .flatMap { traderChangeListener.onUpdateTraderProduct(traderId, product, productId) }
                .flatMap { storedProduct -> productRepo.save(storedProduct) }
                .doOnSuccess {
                    notifyByEmail(Mail(appEmail, trader.email, "Product Update",
                            "Dear ${trader.name}, you have updated details about ${it.name}."))
                }
    }

    override fun deleteTraderProduct(traderId: String, productId: String): Mono<Void> {
        val trader: Trader = findTraderById(traderId).toFuture().join()
        return authenticatedUser.ownsThisAccountById(traderId)
                .map { if (it.not()) throw unAuthorizedAccess() }
                .flatMap { traderChangeListener.onDeleteTraderProduct(traderId, productId) }
                .flatMap { product -> productRepo.delete(product) }
                .doOnSuccess {
                    notifyByEmail(Mail(appEmail, trader.email, "Product Deletion",
                            "Dear ${trader.name}, you have deleted product with id:$productId"))
                }
    }

    override fun deleteTraderCustomer(traderId: String, customerEmail: String): Mono<Void> {
        val trader: Trader = findTraderById(traderId).toFuture().join()
        return authenticatedUser.ownsThisAccountById(traderId)
                .map { if (it.not()) throw unAuthorizedAccess() }
                .flatMap { traderChangeListener.onDeleteTraderCustomer(traderId, customerEmail) }
                .flatMap { customerRepo.delete(it) }
                .doOnSuccess {
                    notifyByEmail(Mail(appEmail, trader.email, "Customer Deletion",
                            "Dear ${trader.name}, you have deleted customer with email: $customerEmail"))
                }
    }

    private fun notifyByEmail(notification: Mail) {
        mailService.apply { sendText(notification) }
    }

    private fun unAuthorizedAccess(): RuntimeException {
        return RuntimeException(HttpStatus.UNAUTHORIZED.reasonPhrase)
    }

}

