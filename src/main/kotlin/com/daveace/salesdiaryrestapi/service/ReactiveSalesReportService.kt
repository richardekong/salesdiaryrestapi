package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

interface ReactiveSalesReportService {

    fun generateReportInExcel(salesEvents:Flux<SalesEvent>): Mono<ByteArrayInputStream>

    fun generateReportInPDF(salesEvents: Flux<SalesEvent>)
}