package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.repository.ReactiveCustomerRepository
import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.hateoas.Link
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ReactiveUserServiceImpl : ReactiveUserService {
    @Autowired
    private lateinit var repo: ReactiveUserRepository

    @Autowired
    private lateinit var customerRepo: ReactiveCustomerRepository

    @Autowired
    private lateinit var traderRepo: ReactiveTraderRepository

    @Autowired
    private lateinit var encoderService: SalesDiaryPasswordEncoderService

    @Autowired
    private lateinit var tokenUtil: TokenUtil

    @Autowired
    private lateinit var authenticatedUser: AuthenticatedUser

    @Autowired
    private lateinit var mailService: MailService

    @Value("\${mailgun.api.email}")
    private lateinit var appEmail: String

    override fun create(user: User): Mono<User> {
        return repo.existsById(user.email)
                .filter { userExists ->
                    when {
                        userExists -> throw RuntimeException(
                                "Account with ${user.email} exists!")
                        else -> userExists.not()
                    }
                }
                .flatMap {
                    repo.save(user).flatMap {
                        val insertedUser: Mono<User> = saveMoreDetails(it)
                        insertedUser
                    }
                }
                .doOnSuccess {
                    val mail = Mail(appEmail, it.email,
                            "Sale Diary Sign up",
                            "welcome to Sales Diary Service")
                    mailService.sendText(mail)
                }
    }

    override fun save(user: User): Mono<User> {
        return repo.save(user)
    }

    override fun findByUsername(email: String): Mono<UserDetails> {
        return findUserByEmail(email).map { it as UserDetails }
    }

    override fun findUserByEmail(email: String): Mono<User> {
        return repo.findById(email)
    }

    override fun findAll(): Flux<User> {
        return repo.findAll()
    }

    override fun updateUser(email: String, user: User): Mono<User> {
        return authenticatedUser.ownsThisAccount(email)
                .flatMap {
                    if (user.phone.isNotEmpty())
                        it.phone = user.phone
                    if (user.customer != null)
                        updateCustomer(user.customer!!)
                    if (user.trader != null)
                        updateTrader(user.trader!!)
                    repo.save(it)
                }
    }

    override fun sendPasswordResetLink(email: String, monoLink: Mono<Link>): Mono<String> {
        return monoLink
                .flatMap {
                    val passwordResetLinkMail = Mail(appEmail, email, "Password Reset Link", it.href)
                    mailService.sendText(passwordResetLinkMail)
                    Mono.just("Password reset mail has been sent")
                }
                .doOnError {
                    throw RuntimeException(it.message)
                }

    }

    override fun resetUserPassword(token: String, newPassword: String): Mono<User> {
        val email: String = tokenUtil.getEmailFromToken(token)
        return repo.findById(email)
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException("Invalid token!")
                })
                .flatMap {
                    if (newPassword.isNotEmpty())
                        it.userPassword = encoderService.encode(newPassword)
                    repo.save(it)
                }
    }

    override fun deleteUserByEmail(email: String): Mono<Void> {
        return authenticatedUser.ownsThisAccount(email)
                .flatMap { user ->
                    repo.delete(user)
                            .flatMap {
                                when {
                                    user.customer != null ->
                                        customerRepo.delete(user.customer!!)
                                    else ->
                                        traderRepo.delete(user.trader!!)
                                }
                            }
                }
    }

    private fun saveMoreDetails(user: User): Mono<User> {
        return when {
            user.trader != null -> saveTraderFromUser(user)
            user.customer != null -> saveCustomerFromUser(user)
            else -> Mono.just(user)
        }
    }

    private fun saveCustomerFromUser(user: User): Mono<User> {
        val customer: Customer? = user.customer
        customer!!.email = user.email
        return customerRepo.save(customer)
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException("Could not save user as customer")
                })
                .map { user }
    }

    private fun saveTraderFromUser(user: User): Mono<User> {
        val trader: Trader? = user.trader
        trader!!.email = user.email
        return traderRepo.save(trader)
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException("Could not save user as trader")
                })
                .map { user }
    }

    private fun updateCustomer(customer: Customer) {
        customerRepo.findById(customer.email)
                .subscribe {
                    if (customer.name.isNotEmpty())
                        it.name = customer.name
                    if (customer.address.isNotEmpty())
                        it.address = customer.address
                    if (customer.company.isNotEmpty())
                        it.company = customer.company
                    if (customer.location.isNotEmpty())
                        it.location = customer.location
                    customerRepo.save(it)
                }
    }

    private fun updateTrader(trader: Trader) {
        traderRepo.findById(trader.email)
                .subscribe {
                    if (trader.name.isNotEmpty())
                        it.name = trader.name
                    if (trader.address.isNotEmpty())
                        it.address = trader.address
                    if (trader.location.isNotEmpty())
                        it.location = trader.location
                    traderRepo.save(it)
                }
    }


}


