package com.daveace.salesdiaryrestapi.configuration

import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.accept.ContentNegotiationManager
import org.springframework.web.reactive.accept.RequestedContentTypeResolverBuilder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver
import org.thymeleaf.context.IContext
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver
import reactor.core.publisher.Mono

@Configuration
@EnableWebFlux
class WebfluxConfig : WebFluxConfigurer, WebFilter {

    @Autowired
    private lateinit var contentNegotiationManager:ContentNegotiationManager

    override fun filter(p0: ServerWebExchange, p1: WebFilterChain): Mono<Void> {

        val reactiveDateContext: ReactiveDataDriverContextVariable = ReactiveDataDriverContextVariable(User("", ""))
        p0.attributes["user_data"] = User("email", "password")
        val template: SpringWebFluxTemplateEngine = SpringWebFluxTemplateEngine()
        val context: SpringWebFluxContext = SpringWebFluxContext(p0)
        template.process("", context)
        return p1.filter(p0)
    }

//    override fun configureContentTypeResolver(builder: RequestedContentTypeResolverBuilder) {
//        super.configureContentTypeResolver(builder)
//        val mediaTypes: Array<MediaType> = arrayOf(
//                MediaType.TEXT_HTML,
//                MediaType.valueOf("application/pdf"),
//                MediaType.valueOf("application/vnd.ms-excel"),
//                MediaType.APPLICATION_XML,
//                MediaType.APPLICATION_JSON
//        )
//        builder.fixedResolver(*mediaTypes)
//    }


//
//    @Bean
//    fun contentNegotiatingViewResolver():ContentNegotiatingViewResolver{
//        val viewResolver = ContentNegotiatingViewResolver()
//        viewResolver.contentNegotiationManager = contentNegotiationManager
//        return viewResolver
//    }
}