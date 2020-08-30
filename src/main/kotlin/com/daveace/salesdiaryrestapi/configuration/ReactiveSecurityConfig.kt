package com.daveace.salesdiaryrestapi.configuration

import com.daveace.salesdiaryrestapi.authentication.SalesDiaryReactiveAuthenticationManager
import com.daveace.salesdiaryrestapi.authentication.SalesDiarySecurityContextRepository
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_WILD_CARD
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class ReactiveSecurityConfig {

    @Autowired
    private lateinit var authenticationManager: SalesDiaryReactiveAuthenticationManager
    @Autowired
    private lateinit var securityContextRepository: SalesDiarySecurityContextRepository
    private val permittedUrls: Array<String> = arrayOf("$API$SALES_DIARY_AUTH_WILD_CARD",
            "/api/sales-diary/events/reports.xlsx")

    @Bean
    fun securityWebFilterChain(http:ServerHttpSecurity):SecurityWebFilterChain?{
        return http
                .exceptionHandling()
                .authenticationEntryPoint{swe,e ->
                    Mono.fromRunnable {
                        swe.response.statusCode =HttpStatus.UNAUTHORIZED
                        Mono.error<AuthenticationException>(e).subscribe()
                    }
                }
                .accessDeniedHandler{swe, e ->
                    Mono.fromRunnable {
                        swe.response.statusCode = HttpStatus.FORBIDDEN
                        Mono.error<AuthenticationException>(e).subscribe()
                    }
                }
                .and()
                .csrf()?.disable()
                ?.httpBasic()?.disable()
                ?.authenticationManager(authenticationManager)
                ?.securityContextRepository(securityContextRepository)
                ?.authorizeExchange()
                ?.pathMatchers(HttpMethod.OPTIONS)?.permitAll()
                ?.pathMatchers(*permittedUrls)?.permitAll()
                ?.anyExchange()?.authenticated()
                ?.and()
                ?.build()
    }

}