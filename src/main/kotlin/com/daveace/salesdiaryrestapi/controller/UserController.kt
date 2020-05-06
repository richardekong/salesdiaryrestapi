package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.config.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_LOGIN_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_PASSWORD_RESET_LINK
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_RESET_PASSWORD
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_SIGN_UP_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USERS
import com.daveace.salesdiaryrestapi.domain.PASSWORD_SIZE_VAL_MSG
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.hateoas.assembler.UserModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.linking.ReactiveLinkSuport
import com.daveace.salesdiaryrestapi.hateoas.model.TokenModel
import com.daveace.salesdiaryrestapi.hateoas.model.UserModel
import com.daveace.salesdiaryrestapi.hateoas.paging.Paginator
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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@RequestMapping(API)
class UserController() : ReactiveLinkSuport {

    private lateinit var userService: ReactiveUserService
    private lateinit var encoderService: SalesDiaryPasswordEncoderService
    private lateinit var tokenUtil: TokenUtil
    private lateinit var paginator: Paginator
    private lateinit var sortProps: SortConfigurationProperties
    private lateinit var authenticatedUser: AuthenticatedUser

    companion object {
        const val WRONG_CREDENTIAL = "Wrong email or password!"
        const val DEFAULT_SIZE = "1"
        const val DEFAULT_PAGE = "0"
        const val DEFAULT_SORT_FIELD = "email"
        const val DEFAULT_SORT_ORDER = "asc"
    }

    @Autowired
    constructor(userService: ReactiveUserService, encoderService: SalesDiaryPasswordEncoderService) : this() {
        this.userService = userService
        this.encoderService = encoderService
    }

    @Autowired
    fun setTokenUtil(tokenUtil: TokenUtil) {
        this.tokenUtil = tokenUtil
    }

    @Autowired
    fun setPaginator(paginator: Paginator) {
        this.paginator = paginator
    }

    @Autowired
    fun setSortProps(sortProps: SortConfigurationProperties) {
        this.sortProps = sortProps
    }

    @Autowired
    fun setAuthenticatedUser(authenticatedUser: AuthenticatedUser) {
        this.authenticatedUser = authenticatedUser
    }

    @PostMapping(SALES_DIARY_AUTH_SIGN_UP_USERS, produces = ["application/json"])
    @ResponseStatus(value = HttpStatus.CREATED)
    fun signUpUser(@RequestBody @Valid user: User): Mono<UserModel> {
        val hash = encoderService.encode(user.userPassword)
        user.userPassword = hash
        return userService.create(user)
                .flatMap {
                    respondWithReactiveLink(
                            UserModel(it),
                            methodOn(this.javaClass).signUpUser(it)
                    )
                }
    }

    @PostMapping(SALES_DIARY_AUTH_LOGIN_USERS)
    fun logInUser(@RequestBody credentials: Map<String, String>): Mono<TokenModel> {
        val email: String? = credentials["email"]
        val password: String? = credentials["password"]
        return userService.findUserByEmail(email!!)
                .switchIfEmpty(Mono.fromRunnable {
                    throw AuthenticationException(
                            HttpStatus.UNAUTHORIZED.reasonPhrase)
                })
                .map {
                    if (!encoderService.matches(password, it.password))
                        throw RuntimeException(WRONG_CREDENTIAL)
                    tokenUtil.generateToken(it)
                }
                .flatMap {
                    respondWithReactiveLink(
                            TokenModel(it),
                            methodOn(this::class.java)
                                    .logInUser(credentials)
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

    @PatchMapping("$SALES_DIARY_USER{email}")
    fun updateUser(@PathVariable email: String,
                   @RequestBody user: User): Mono<UserModel> {
        return userService.updateUser(email, user)
                .flatMap {
                    respondWithReactiveLink(
                            UserModel(it),
                            methodOn(this::class.java)
                                    .updateUser(email, it))
                }

    }

    @GetMapping("$SALES_DIARY_AUTH_PASSWORD_RESET_LINK{email}")
    fun requestPasswordResetLink(@PathVariable email: String): Mono<String> {
        return userService.findUserByEmail(email)
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException("Wrong email address!")
                })
                .flatMap {
                    val token: String = tokenUtil.generateToken(it, validity = 30000L)
                    val monoLink: Mono<Link> = linkTo(methodOn(
                            this.javaClass).resetPassword(token, ""))
                            .withSelfRel().toMono()
                    userService.sendPasswordResetLink(email, monoLink)
                }
    }

    @PatchMapping("$SALES_DIARY_AUTH_RESET_PASSWORD{token}")
    fun resetPassword(
            @PathVariable token: String,
            @RequestParam(name = "password")
            @Size(message = PASSWORD_SIZE_VAL_MSG)
            password: String): Mono<UserModel> {

        return userService.resetUserPassword(token, password)
                .flatMap { user ->
                    respondWithReactiveLink(
                            UserModel(user),
                            methodOn(this.javaClass)
                                    .resetPassword(token, password))
                }
    }


    @DeleteMapping("$SALES_DIARY_USER{email}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable email: String): Mono<Void> {
        return userService.deleteUserByEmail(email)
    }

}

