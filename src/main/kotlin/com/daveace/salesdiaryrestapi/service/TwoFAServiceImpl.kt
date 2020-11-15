package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.TwoFAData
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class TwoFAServiceImpl : TwoFAService {

    private lateinit var userRepo: ReactiveUserRepository
    private lateinit var smsService: SMSService
    private lateinit var mailService: MailService
    private lateinit var mailTemplatingService: MailTemplatingService
    private lateinit var twoFAMessageService: TwoFAMessageService
    private lateinit var encoderService: SalesDiaryPasswordEncoderService

    @Autowired
    fun initUserRepo(userRepo: ReactiveUserRepository) {
        this.userRepo = userRepo
    }

    @Autowired
    fun initSMSService(smsService: SMSService) {
        this.smsService = smsService
    }

    @Autowired
    fun initMailService(mailService: MailService) {
        this.mailService = mailService
    }

    @Autowired
    fun initMailTemplatingService(mailTemplatingService: MailTemplatingService) {
        this.mailTemplatingService = mailTemplatingService
    }

    @Autowired
    fun initTwoFAMessageService(twoFAMessageService: TwoFAMessageService) {
        this.twoFAMessageService = twoFAMessageService
    }

    @Autowired
    fun initEncoderService(encoderService: SalesDiaryPasswordEncoderService) {
        this.encoderService = encoderService
    }

    override fun activate2FA(user: User, channel: String, swe: ServerWebExchange): Mono<User> {
        val randomCode: String = generateRandomCode()
        val encoded2FACode: String = encoderService.encode(randomCode)
        return userRepo.save(user.apply {
            twoFAData.enabled = true
            twoFAData.code = encoded2FACode
            twoFAData.channel = channel
        }).doOnSuccess {
            twoFAMessageService.apply {
                send(it, randomCode, swe)
            }
        }
    }

    override fun deActivate2FA(user: User, code: String): Mono<User> {
        return userRepo.save(user.apply {
            if (code.isBlank())
                throw AuthenticationException("Provide a 2FA code")
            if (!encoderService.matches(code, twoFAData.code))
                throw AuthenticationException(
                        HttpStatus.UNAUTHORIZED.reasonPhrase)
            if (twoFAData.enabled) {
                twoFAData.enabled = false
            }
        })
    }

    override fun verify2FACode(email: String, code: String): Mono<User> {
        return userRepo.findUserByEmail(email).map { userToVerify ->
            userToVerify.twoFAData.let {
                verify2FACodeFrom2FAData(it, code)
                userToVerify
            }
        }
    }

    override fun request2FACode(user: User, swe: ServerWebExchange) {
        val randomCode: String = generateRandomCode()
        Mono.fromRunnable<Runnable> {
            user.twoFAData.apply {
                if (enabled) {
                    code = encoderService.encode(randomCode)
                    validity = LocalDateTime.now().plusMinutes(TwoFAData.TIME_FRAME)
                    userRepo.save(user).subscribe({ updatedUser ->
                        twoFAMessageService.send(updatedUser, randomCode, swe)
                    }, { error ->
                        throw RuntimeException(error.message)
                    })
                }
            }
        }.subscribe({}, {
            throw RuntimeException(it.message)
        })
    }

    override fun update2FAChannel(user: User, channel: String, code: String): Mono<User> {
        verify2FACodeFrom2FAData(user.twoFAData, code)
        return userRepo.save(user.apply {
            if (channel !in TwoFAData.Channel.channels())
                throw RuntimeException(HttpStatus.EXPECTATION_FAILED.reasonPhrase)
            twoFAData.channel = channel
        })

    }

    private fun verify2FACodeFrom2FAData(it: TwoFAData, code: String) {
        if (it.enabled && it.isExpired())
            throw AuthenticationException("2FA code has expired, request new code")
        if (it.enabled && code.isBlank())
            throw AuthenticationException("Provide a 2FA code")
        if (it.enabled && !encoderService.matches(code, it.code))
            throw AuthenticationException("Wrong 2FA code!")
    }
}

