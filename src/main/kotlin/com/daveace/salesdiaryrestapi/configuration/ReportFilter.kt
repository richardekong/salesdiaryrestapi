package com.daveace.salesdiaryrestapi.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Configuration
class ReportFilter : WebFilter {

    companion object {
        const val EXCEL_EXTENSION = ".xlsx"
        const val PDF_EXTENSION = ".pdf"
        const val REPORT = "report"
        const val EXCEL_HEADER_VALUE = "attachment;filename=$REPORT$EXCEL_EXTENSION"
        const val EXCEL_MEDIA_TYPE = "application/vnd.ms-excel"
    }

    override fun filter(exchange: ServerWebExchange, filterChain: WebFilterChain): Mono<Void> {
        modifyResponseHeaders(exchange)
        return filterChain.filter(exchange)
    }

    private fun modifyResponseHeaders(exchange: ServerWebExchange) {
        if (exchange.request.uri.path.contains(EXCEL_EXTENSION, true)) {
            exchange.response.headers.let {
                it.add(HttpHeaders.CONTENT_DISPOSITION, EXCEL_HEADER_VALUE)
                it.contentType = MediaType.parseMediaType(EXCEL_MEDIA_TYPE)
            }
        }
        if (exchange.request.uri.path.contains(PDF_EXTENSION, true)) {
            exchange.request.headers.let {
                it.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=$REPORT$PDF_EXTENSION")
                it.contentType = MediaType.APPLICATION_PDF
            }
        }
    }

}