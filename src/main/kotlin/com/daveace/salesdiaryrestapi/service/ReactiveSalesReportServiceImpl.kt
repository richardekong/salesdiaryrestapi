package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.configuration.ReportFilter
import com.daveace.salesdiaryrestapi.domain.SalesMetrics
import com.daveace.salesdiaryrestapi.mapper.Mappable
import com.daveace.salesdiaryrestapi.report.ExcelGenerator
import com.daveace.salesdiaryrestapi.report.HTMLToPDFGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

@Service
class ReactiveSalesReportServiceImpl : ReactiveSalesReportService {

    private lateinit var pdfGenerator: HTMLToPDFGenerator

    @Autowired
    fun initPdfGenerator(pdfGenerator: HTMLToPDFGenerator) {
        this.pdfGenerator = pdfGenerator
    }

    override fun <T> generateReport(exchange: ServerWebExchange, data: Flux<T>): Mono<ByteArrayInputStream> where T:Mappable{
        val path: String = exchange.request.uri.path
        return when {
            path.contains(ReportFilter.EXCEL_EXTENSION) -> ExcelGenerator.generateExcel(data)
            path.contains(ReportFilter.PDF_EXTENSION) -> pdfGenerator.generatePDF(data, exchange)
            else -> throw RuntimeException(HttpStatus.UNSUPPORTED_MEDIA_TYPE.toString())
        }
    }

    override fun generateExtendedReport(exchange: ServerWebExchange, data: Mono<Map<String?, List<Mappable>>>): Mono<ByteArrayInputStream> {
        return exchange.run {
            val path: String = exchange.request.uri.path
            when {
                path.contains(ReportFilter.EXCEL_EXTENSION) -> ExcelGenerator.generateExcel(data)
                path.contains(ReportFilter.PDF_EXTENSION) -> pdfGenerator.generatePDF(this, data)
                else -> throw RuntimeException(HttpStatus.UNSUPPORTED_MEDIA_TYPE.toString())
            }
        }
    }

}

