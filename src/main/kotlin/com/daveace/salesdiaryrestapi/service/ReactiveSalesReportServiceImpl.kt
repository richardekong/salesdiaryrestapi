package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.report.ExcelGenerator
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

@Service
class ReactiveSalesReportServiceImpl : ReactiveSalesReportService {

    override fun generateReportInExcel(salesEvents: Flux<SalesEvent>): Mono<ByteArrayInputStream> {
        return ExcelGenerator.generateExcel(salesEvents)
    }

    override fun generateReportInPDF(salesEvents: Flux<SalesEvent>) {
        TODO("Not yet implemented")
    }

}