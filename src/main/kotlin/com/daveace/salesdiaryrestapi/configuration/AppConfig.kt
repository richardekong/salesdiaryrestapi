package com.daveace.salesdiaryrestapi.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource
import org.springframework.format.datetime.DateFormatter
import org.springframework.format.datetime.DateFormatterRegistrar
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory
import org.springframework.format.support.DefaultFormattingConversionService
import org.springframework.format.support.FormattingConversionService

@Configuration
class AppConfig{

    @Bean
    fun conversionService():FormattingConversionService{
        return DefaultFormattingConversionService(false).apply {
            addFormatterForFieldAnnotation(NumberFormatAnnotationFormatterFactory())
            val registrar = DateFormatterRegistrar()
            registrar.setFormatter(DateFormatter("dd-MM-yyyy"))
            registrar.registerFormatters(this)
        }
    }
}

@Bean
fun getPropertySourcesPlaceHolderConfigurer():PropertySourcesPlaceholderConfigurer{
    val pspc = PropertySourcesPlaceholderConfigurer()
    pspc.setLocation(ClassPathResource("application.properties"))
    pspc.setIgnoreUnresolvablePlaceholders(true)
    return pspc
}