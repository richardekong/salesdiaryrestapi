package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

interface ReactiveSalesReportService {
    companion object{
        const val EXCEL_EXTENSION:String = ".xls"
        const val PDF_EXTENSION:String = ".pdf"
    }

    fun generateReport(salesEvents: Flux<SalesEvent>, fileExtension:String=EXCEL_EXTENSION):ByteArrayInputStream

    fun generateReportInExcel(salesEvents:Flux<SalesEvent>): Mono<ByteArrayInputStream>

    fun generateReportInPDF(salesEvents: Flux<SalesEvent>)
}