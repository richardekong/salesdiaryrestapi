package com.daveace.salesdiaryrestapi.report


import com.daveace.salesdiaryrestapi.mapper.Mappable
import com.itextpdf.html2pdf.HtmlConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import org.springframework.web.server.ServerWebExchange
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.stream.Collectors.toList
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Service
class HTMLToPDFGenerator {

    companion object {
        const val DATA = "data"
        const val HEADER = "header"
        const val HEADERS = "headers"
        const val GROUPED_DATA = "grouped_data"
        const val SALES_EVENT_REPORT_TEMPLATE = "sales_event_report_template"
        const val OUTPUT_FILE_PATH = "${ResourceUtils.CLASSPATH_URL_PREFIX}output/report.pdf"

    }

    private lateinit var templateEngine: SpringWebFluxTemplateEngine

    @Autowired
    fun initTemplateEngine(templateEngine: SpringWebFluxTemplateEngine) {
        this.templateEngine = templateEngine
    }

    fun <T : Any> generatePDF(report: Flux<T>, swe: ServerWebExchange): Mono<ByteArrayInputStream> {
        return report.collectList().map {
            val context: SpringWebFluxContext = SpringWebFluxContext(swe, swe.localeContext.locale).apply {
                setVariable(HEADER, createHeaderLabels(it[0]::class))
                setVariable(DATA, it)
            }
            generatePDFFromHTML(templateEngine.process(SALES_EVENT_REPORT_TEMPLATE, context))
        }

    }

    fun generatePDF(swe: ServerWebExchange, groupedData: Mono<Map<String?, List<Mappable>>>): Mono<ByteArrayInputStream> {
        return groupedData.map {
            val context = SpringWebFluxContext(swe, swe.localeContext.locale).apply {
                setVariable(HEADERS, it.values.groupBy { data -> data[0]::class.simpleName }
                        .map { mapOf(it.key to createHeaderLabels(it.value[0]::class)) }.toList())
                setVariable(GROUPED_DATA, it)
                println(it)
            }
            generatePDFFromHTML(templateEngine.process(SALES_EVENT_REPORT_TEMPLATE, context))
        }
    }

    private fun generatePDFFromHTML(HTMLString: String): ByteArrayInputStream {
        val outputStream = FileOutputStream(ResourceUtils.getFile(OUTPUT_FILE_PATH))
        HtmlConverter.convertToPdf(HTMLString, outputStream)
        return FileInputStream(ResourceUtils.getFile(OUTPUT_FILE_PATH)).run {
            ByteArrayInputStream(this.readBytes())
        }
    }

    private fun <T : Any> createHeaderLabels(kClass: KClass<T>): List<String> {
        return kClass.memberProperties.parallelStream().map { field ->
            field.isAccessible = true
            val label: String = field.name.toUpperCase()
            field.isAccessible = false
            label
        }.collect(toList())
    }

}