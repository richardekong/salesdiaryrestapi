package com.daveace.salesdiaryrestapi.service

import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

interface ReactiveSalesReportService {
    companion object{
        const val EXCEL_EXTENSION = ".xlsx"
        const val PDF_EXTENSION = ".pdf"
    }

    fun<T:Any> generateReport(exchange: ServerWebExchange, data:Flux<T>): Mono<ByteArrayInputStream>
}