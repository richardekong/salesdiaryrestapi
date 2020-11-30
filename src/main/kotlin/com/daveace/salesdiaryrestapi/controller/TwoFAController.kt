package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.DEACTIVATED_2FA
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_LOGIN_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.TWO_FACTOR_AUTH
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.TWO_FACTOR_AUTH_CHANNEL
import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.hateoas.model.MessageModel
import com.daveace.salesdiaryrestapi.hateoas.model.UserModel
import com.daveace.salesdiaryrestapi.repository.InMemoryTokenStore
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import com.daveace.salesdiaryrestapi.service.SalesDiaryPasswordEncoderService
import com.daveace.salesdiaryrestapi.service.TwoFAService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping(API)
class TwoFAController : BaseController() {

    private lateinit var twoFAService: TwoFAService
    private lateinit var userRepo: ReactiveUserRepository
    private lateinit var encoderService: SalesDiaryPasswordEncoderService

    companion object {
        const val TWO_FA_ACTIVATION_MAIL_TEMPLATE = "2fa_activation_mail_template"
        const val TWO_FA_DEACTIVATION_MAIL_TEMPLATE = "2fa_deactivation_mail_template"
    }

    @Autowired
    fun initUserRepo(userRepo: ReactiveUserRepository) {
        this.userRepo = userRepo
    }

    @Autowired
    fun initTwoFAService(twoFAService: TwoFAService) {
        this.twoFAService = twoFAService
    }

    @Autowired
    fun initEncoderService(encoderService: SalesDiaryPasswordEncoderService) {
        this.encoderService = encoderService
    }

    @PatchMapping("$SALES_DIARY_USERS$TWO_FACTOR_AUTH")
    fun activate2FA(
            swe: ServerWebExchange,
            @RequestParam("channel", defaultValue = "email")
            channel: String): Mono<UserModel> {
        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            twoFAService.activate2FA(currentUser, channel, swe).flatMap { updatedUser ->
                respondWithReactiveLink(
                        UserModel(updatedUser),
                        methodOn(this.javaClass).activate2FA(swe, channel))
            }.doOnSuccess {
                val subject = "2FA Activation"
                prepareAndSendEmailWithHTMLTemplate(
                        TWO_FA_ACTIVATION_MAIL_TEMPLATE,
                        Mail(currentUser.email, subject),
                        currentUser.toMap(), swe)
                InMemoryTokenStore.revokeToken(currentUser.email)
            }
        }

    }

    @PatchMapping("$SALES_DIARY_USERS$DEACTIVATED_2FA")
    fun deactivate2FA(
            @RequestHeader("code", defaultValue = "") code: String,
            swe: ServerWebExchange): Mono<UserModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            twoFAService.deActivate2FA(currentUser, code).flatMap { updatedUser ->
                respondWithReactiveLink(
                        UserModel(updatedUser),
                        methodOn(this.javaClass).deactivate2FA(encoderService.encode(code), swe))
            }.doOnSuccess {
                val subject = "2FA Deactivation"
                prepareAndSendEmailWithHTMLTemplate(
                        TWO_FA_DEACTIVATION_MAIL_TEMPLATE,
                        Mail(currentUser.email, subject),
                        currentUser.toMap(), swe)
            }
        }
    }

    @GetMapping("$SALES_DIARY_AUTH_LOGIN_USERS/{email}$TWO_FACTOR_AUTH")
    fun request2FACode(@PathVariable email: String,
                       swe: ServerWebExchange): Mono<MessageModel> {
        return userRepo.findUserByEmail(email).flatMap {
            twoFAService.request2FACode(it, swe)
            val message = MessageModel("check your inbox for a verification code")
            respondWithReactiveLink(message, methodOn(this.javaClass)
                    .request2FACode(email, swe))
        }.onErrorMap {
            throw RuntimeException(it.message)
        }

    }

    @PatchMapping("$SALES_DIARY_USERS$TWO_FACTOR_AUTH_CHANNEL")
    fun update2FAChannel(
            @RequestParam("channel", defaultValue = "") channel: String,
            @RequestHeader("code", defaultValue = "") code: String): Mono<UserModel> {

        return authenticatedUser.getCurrentUser().flatMap {
            twoFAService.update2FAChannel(it, channel, code)
        }.flatMap {
            respondWithReactiveLink(UserModel(it), methodOn(this.javaClass).update2FAChannel(
                    channel, it.twoFAData.code))
        }
    }

}

