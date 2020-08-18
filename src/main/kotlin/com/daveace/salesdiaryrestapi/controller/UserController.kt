package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_LOGIN_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_PASSWORD_RESET_LINK
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_RESET_PASSWORD
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_SIGN_UP_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USERS
import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.domain.PASSWORD_SIZE_VAL_MSG
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.hateoas.assembler.UserModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.model.TokenModel
import com.daveace.salesdiaryrestapi.hateoas.model.UserModel
import com.daveace.salesdiaryrestapi.repository.InMemoryTokenStore
import com.daveace.salesdiaryrestapi.service.ReactiveUserService
import com.daveace.salesdiaryrestapi.service.SalesDiaryPasswordEncoderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@RequestMapping(API)
class UserController() : BaseController() {

    private lateinit var userService: ReactiveUserService
    private lateinit var encoderService: SalesDiaryPasswordEncoderService

    companion object {
        const val WRONG_CREDENTIAL = "Wrong email or password!"
        const val DEFAULT_SIZE = "1"
        const val DEFAULT_PAGE = "0"
        const val DEFAULT_SORT_FIELD = "id"
        const val DEFAULT_SORT_ORDER = "asc"
        const val SIGN_UP_MAIL_TEMPLATE = "sign_up_mail_template"
        const val PASSWORD_RESET_LINK_MAIL_TEMPLATE = "password_reset_link_mail_template"
        const val PASSWORD_RESET_MAIL_TEMPLATE = "password_reset_mail_template"
        const val ACCOUNT_DELETION_MAIL_TEMPLATE = "account_deletion_mail_template"
        const val LOG_IN_MAIL_TEMPLATE = "log_in_mail_template"
    }

    @Autowired
    constructor(userService: ReactiveUserService, encoderService: SalesDiaryPasswordEncoderService) : this() {
        this.userService = userService
        this.encoderService = encoderService
    }

    @PostMapping(SALES_DIARY_AUTH_SIGN_UP_USERS, produces = ["application/json"])
    @ResponseStatus(value = HttpStatus.CREATED)
    fun signUpUser(@RequestBody @Valid user: User, exchange: ServerWebExchange): Mono<UserModel> {
        val hash = encoderService.encode(user.userPassword)
        user.userPassword = hash
        return userService.create(user)
                .doOnSuccess {
                    val subject = "Sales Diary Sign up"
                    prepareAndSendEmailWithHTMLTemplate(
                            SIGN_UP_MAIL_TEMPLATE,
                            Mail(it.email, subject),
                            it.toMap(),
                            exchange
                    )
                }.flatMap {
                    respondWithReactiveLink(
                            UserModel(it),
                            methodOn(this.javaClass).signUpUser(it, exchange)
                    )
                }
    }

    @PostMapping(SALES_DIARY_AUTH_LOGIN_USERS)
    fun logInUser(@RequestBody credentials: Map<String, String>, exchange: ServerWebExchange): Mono<TokenModel> {
        val email: String? = credentials["email"]
        val password: String? = credentials["password"]
        return userService.findUserByEmail(email!!)
                .switchIfEmpty(Mono.fromRunnable {
                    throw AuthenticationException(
                            HttpStatus.UNAUTHORIZED.reasonPhrase)
                })
                .filter { encoderService.matches(password, it.password) }
                .switchIfEmpty(Mono.fromRunnable {
                    throw RuntimeException(WRONG_CREDENTIAL)
                })
                .map { tokenUtil.generateToken(it) }
                .doOnSuccess {
                    val subject = "Log In Confirmation"
                    val accessTimeKey = "access_time"
                    val userKey = "user"
                    val resetLinkKey = "reset_link"
                    val currentUser: User = tokenUtil.getUserFromToken(it)
                    val resetLink: String = linkTo(methodOn(this.javaClass)
                            .resetPassword(it, "\u0020", exchange))
                            .withSelfRel().toMono().toFuture().join().href

                    val recipientData: MutableMap<String, Any?> = mutableMapOf(
                            accessTimeKey to LocalDateTime.now().format(DateTimeFormatter
                                    .ofPattern(DATE_TIME_PATTERN)),
                            resetLinkKey to resetLink,
                            userKey to currentUser)
                    prepareAndSendEmailWithHTMLTemplate(
                            LOG_IN_MAIL_TEMPLATE, Mail(email, subject),
                            recipientData, exchange)
                }
                .flatMap {
                    respondWithReactiveLink(
                            TokenModel(it),
                            methodOn(this::class.java)
                                    .logInUser(credentials, exchange)
                    )
                }
    }

    @GetMapping("$SALES_DIARY_USER{email}")
    fun findUserByEmail(@PathVariable email: String): Mono<UserModel> {
        return userService.findUserByEmail(email)
                .flatMap { user ->
                    respondWithReactiveLink(
                            UserModel(user),
                            methodOn(this::class.java)
                                    .findUserByEmail(email)
                    )
                }
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException(HttpStatus.NOT_FOUND.reasonPhrase)
                })
    }

    @GetMapping(SALES_DIARY_USERS)
    fun findAllUser(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<UserModel>> {
        val pageRequest: PageRequest = PageRequest.of(page, size)
        sortProps.by = by
        sortProps.dir = dir
        val link: Link = linkTo(methodOn(this.javaClass)
                .findAllUser(size, page, by, dir))
                .withSelfRel()
                .toMono().toFuture().join()
        return paginator.paginate(
                UserModelAssembler(),
                userService.findAll(),
                pageRequest,
                link,
                sortProps
        )
    }

    @GetMapping("$SALES_DIARY_AUTH_PASSWORD_RESET_LINK{email}")
    fun requestPasswordResetLink(@PathVariable email: String, exchange: ServerWebExchange): Mono<String> {
        return userService.findUserByEmail(email)
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException("Wrong email address!")
                }).flatMap {
                    val token: String = tokenUtil.generateToken(it, validity = 60000L)
                    linkTo(methodOn(this.javaClass)
                            .resetPassword(token, "\u0020", exchange))
                            .withSelfRel()
                            .toMono()
                            .flatMap { passwordResetLink ->
                                val subject = "Password Reset Link"
                                val linkKey = "link"
                                val userKey = "user"
                                val recipientData: MutableMap<String, Any?> = mutableMapOf(
                                        linkKey to passwordResetLink.href,
                                        userKey to it)
                                prepareAndSendEmailWithHTMLTemplate(
                                        PASSWORD_RESET_LINK_MAIL_TEMPLATE,
                                        Mail(it.email, subject),
                                        recipientData, exchange)
                            }
                }
    }

    @PatchMapping("$SALES_DIARY_AUTH_RESET_PASSWORD{token}")
    fun resetPassword(
            @PathVariable token: String,
            @RequestParam(name = "password")
            @Size(message = PASSWORD_SIZE_VAL_MSG)
            password: String, exchange: ServerWebExchange): Mono<UserModel> {

        return userService.resetUserPassword(token, password)
                .doOnSuccess {
                    val subject = "Password Reset Operation"
                    prepareAndSendEmailWithHTMLTemplate(
                            PASSWORD_RESET_MAIL_TEMPLATE,
                            Mail(it.email, subject),
                            it.toMap(), exchange)
                }
                .flatMap { user ->
                    respondWithReactiveLink(
                            UserModel(user),
                            methodOn(this.javaClass)
                                    .resetPassword(token, user.password, exchange))
                }
                .doOnSuccess {
                    InMemoryTokenStore.revokeToken(it.email)
                }
    }


    @DeleteMapping("$SALES_DIARY_USER{email}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable email: String, exchange: ServerWebExchange): Mono<Void> {
        return authenticatedUser.isCurrentUserAuthorizedByEmail(email)
                .flatMap {
                    userService.deleteUserByEmail(email)
                            .doOnSuccess {
                                val subject = "Sales Diary Account deletion"
                                prepareAndSendEmailWithHTMLTemplate(
                                        ACCOUNT_DELETION_MAIL_TEMPLATE,
                                        Mail(email, subject),
                                        mutableMapOf(),
                                        exchange)
                                InMemoryTokenStore.revokeToken(email)
                            }
                }
    }

}

