package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.SalesMetrics
import com.daveace.salesdiaryrestapi.mapper.Mappable
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

interface ReactiveSalesReportService {
    companion object {
        const val EXCEL_EXTENSION = ".xlsx"
        const val PDF_EXTENSION = ".pdf"
    }

    fun <T> generateReport(exchange: ServerWebExchange, data: Flux<T>): Mono<ByteArrayInputStream> where T : Mappable

    fun generateExtendedReport(exchange: ServerWebExchange, data: Mono<Map<String?, List<Mappable>>>): Mono<ByteArrayInputStream>
}