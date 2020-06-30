package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.repository.ReactiveCustomerRepository
import com.daveace.salesdiaryrestapi.repository.ReactiveProductRepository
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

    private lateinit var repo: ReactiveUserRepository
    private lateinit var traderRepo: ReactiveTraderRepository
    private lateinit var productRepo: ReactiveProductRepository
    private lateinit var customerRepo: ReactiveCustomerRepository
    private lateinit var encoderService: SalesDiaryPasswordEncoderService
    private lateinit var tokenUtil: TokenUtil

    private lateinit var mailService: MailService

    @Value("\${mailgun.api.email}")
    private lateinit var appEmail: String

    @Autowired
    fun initUserRepository(repo: ReactiveUserRepository) {
        this.repo = repo
    }

    @Autowired
    fun initTraderRepository(traderRepo: ReactiveTraderRepository) {
        this.traderRepo = traderRepo
    }

    @Autowired
    fun initProductRepository(productRepo: ReactiveProductRepository) {
        this.productRepo = productRepo
    }

    @Autowired
    fun initCustomerRepository(customerRepo: ReactiveCustomerRepository) {
        this.customerRepo = customerRepo
    }

    @Autowired
    fun initEncoderService(encoderService: SalesDiaryPasswordEncoderService) {
        this.encoderService = encoderService
    }

    @Autowired
    fun initTokenUtil(tokenUtil: TokenUtil) {
        this.tokenUtil = tokenUtil
    }

    @Autowired
    fun initMailService(mailService: MailService) {
        this.mailService = mailService
    }

    override fun create(user: User): Mono<User> {
        return repo.existsByEmail(user.email).filter { userExists ->
            when {
                userExists -> throw RuntimeException("Account with ${user.email} exists!")
                else -> userExists.not()
            }
        }.flatMap {
            repo.save(user).flatMap { saveMoreDetails(it) }
                    .doOnSuccess {
                        mailService.apply {
                            sendText(Mail(appEmail, it.email, "Sale Diary Sign up",
                                    "welcome to Sales Diary Service"))
                        }
                    }
        }
    }

    override fun save(user: User): Mono<User> {
        return repo.save(user)
    }

    override fun findByUsername(email: String): Mono<UserDetails> {
        return findUserByEmail(email).map { it as UserDetails }
    }

    override fun findUserByEmail(email: String): Mono<User> {
        return repo.findUserByEmail(email)
    }

    override fun findAll(): Flux<User> {
        return repo.findAll()
    }

    override fun sendPasswordResetLink(email: String, monoLink: Mono<Link>): Mono<String> {
        return monoLink.flatMap { passwordResetLink ->
            val passwordResetLinkMail = Mail(appEmail, email, "Password Reset Link", passwordResetLink.href)
            mailService.sendText(passwordResetLinkMail)
        }
    }

    override fun resetUserPassword(token: String, newPassword: String): Mono<User> {
        val id: String = tokenUtil.getIdFromToken(token)
        return repo.findById(id)
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException("Invalid token!")
                })
                .flatMap {
                    if (newPassword.isNotEmpty())
                        it.userPassword = encoderService.encode(newPassword)
                    repo.save(it)
                }.doOnSuccess {
                    mailService.apply {
                        sendText(Mail(appEmail, it.email, "Password Reset Operation",
                                "You have successfully changed your password."))
                    }
                }
    }

    override fun deleteUserByEmail(email: String): Mono<Void> {
        val userId: String = repo.findUserByEmail(email).map { it.id }
                .toFuture().join()
        return repo.findUserByEmail(email)
                .flatMap { user ->
                    repo.delete(user)
                }.doOnSuccess {
                    deleteTraderByUserId(userId)
                    mailService.apply {
                        sendText(Mail(appEmail, email, "Sales Diary Account deletion",
                                "your subscription with Sales Diary Service has been terminated!"))
                    }
                }
    }

    private fun saveMoreDetails(user: User): Mono<User> {
        return when {
            user.trader != null -> saveTraderFromUser(user)
            else -> Mono.just(user)
        }
    }

    private fun saveTraderFromUser(user: User): Mono<User> {
        val trader: Trader? = user.trader
        trader!!.id = user.id
        trader.email = user.email
        return traderRepo.save(trader)
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException("Could not save user as trader")
                })
                .map { user }
    }

    private fun deleteTraderByUserId(userId: String) {
        traderRepo.apply {
            deleteById(userId).doOnSuccess {
                deleteCustomersByUserId(userId)
                deleteProductsByUserId(userId)
            }.subscribe()
        }
    }

    private fun deleteProductsByUserId(userId: String) {
        productRepo.apply {
            findAll().filter { it.traderId == userId }.collectList()
                    .flatMap { deleteAll(it) }.subscribe()
        }
    }

    private fun deleteCustomersByUserId(userId: String) {
        customerRepo.apply {
            findAll().filter { it.traderId == userId }.collectList()
                    .flatMap { deleteAll(it) }.subscribe()
        }
    }

}

