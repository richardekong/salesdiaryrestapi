package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.domain.TwoFAData
import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext
import reactor.core.publisher.Mono

@Service
class TwoFAMessageServiceImpl : TwoFAMessageService {

    private lateinit var mailService: MailService
    private lateinit var gmailService: GmailService
    private lateinit var smsService: SMSService
    private lateinit var voiceService: VoiceService
    private lateinit var mailTemplatingService: MailTemplatingService
    private lateinit var templateEngine: SpringWebFluxTemplateEngine

    companion object {
        const val TWO_FACTOR_CODE_MAIL_TEMPLATE = "2fa_code_template"
        const val USER = "user"
        const val TEXT = "text"
        const val TWO_FA_MAIL_SUBJECT = "2FA Code"
    }

    @Autowired
    fun initMailService(mailService: MailService) {
        this.mailService = mailService
    }

    @Autowired
    fun initGmailService(gmailService: GmailService){
        this.gmailService = gmailService
    }

    @Autowired
    fun initSMSService(smsService: SMSService) {
        this.smsService = smsService
    }

    @Autowired
    fun initVoiceService(voiceService: VoiceService) {
        this.voiceService = voiceService
    }

    @Autowired
    fun initMailTemplateService(mailTemplatingService: MailTemplatingService) {
        this.mailTemplatingService = mailTemplatingService
    }

    @Autowired
    fun initTemplateEngine(templateEngine: SpringWebFluxTemplateEngine) {
        this.templateEngine = templateEngine
    }

    override fun send(user: User, text: String, swe: ServerWebExchange) {
        Mono.fromRunnable<Runnable> {
            user.twoFAData.apply {
                if (channel !in TwoFAData.Channel.channels())
                    throw RuntimeException(HttpStatus.EXPECTATION_FAILED.reasonPhrase)
                if (channel == TwoFAData.Channel.SMS.channel)
                    smsService.send(user.trader?.phone!!, text)
                if (channel == TwoFAData.Channel.VOICE.channel)
                    voiceService.send(user.trader?.phone!!, text)
                if (channel == TwoFAData.Channel.EMAIL.channel)
                    sendEmail(user, text, swe)
            }
        }.subscribe({}, {
            throw RuntimeException(it.message)
        })
    }

    private fun sendEmail(user: User, text: String, swe: ServerWebExchange) {
        val context = SpringWebFluxContext(swe, swe.localeContext.locale)
        val data: MutableMap<String, Any> = mutableMapOf(USER to user, TEXT to text)
        val mail: Mail = mailTemplatingService.createMailFromTemplate(
                data, TWO_FACTOR_CODE_MAIL_TEMPLATE, context).apply {
            to = user.email
            subject = TWO_FA_MAIL_SUBJECT
        }
        //mailService.sendHTML(mail)
        gmailService.sendHTML(mail)
    }
}

