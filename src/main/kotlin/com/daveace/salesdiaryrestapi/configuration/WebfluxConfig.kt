package com.daveace.salesdiaryrestapi.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebfluxConfig:WebFluxConfigurer {

}