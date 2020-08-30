package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Service
class ReactiveSalesReportServiceImpl : ReactiveSalesReportService {

    override fun generateReport(salesEvents: Flux<SalesEvent>, fileExtension: String): Mono<ByteArrayInputStream> {
        TODO("Not yet implemented")
    }

    override fun generateReportInExcel(salesEvents: Flux<SalesEvent>): Mono<ByteArrayInputStream> {
        return salesEvents.collectList().map {
            salesEventsToExcel(it)
        }.onErrorMap {
            throw RuntimeException(it.message)
        }
    }

    override fun generateReportInPDF(salesEvents: Flux<SalesEvent>) {
        TODO("Not yet implemented")
    }

    private fun salesEventsToExcel(salesEvents: MutableList<SalesEvent>): ByteArrayInputStream {
        return ByteArrayInputStream(
                ByteArrayOutputStream().apply {
                    try {
                        val workBook: Workbook = XSSFWorkbook()
                        val sheet: Sheet = workBook.createSheet()
                        setHeader(sheet, createHeaderData(SalesEvent::class))
                        setDataForSheet(sheet, salesEvents)
                        workBook.write(this)
                    } catch (ioe: IOException) {
                        throw RuntimeException(ioe.message)
                    }
                }
                        .toByteArray()
        )

    }

    private fun <T : Any> createHeaderData(kClass: KClass<T>): List<String> {
        return kClass.memberProperties.asSequence()
                .map { member ->
                    member.isAccessible = true
                    member.name.toUpperCase()
                }
                .toList()
    }

    private fun setHeader(sheet: Sheet, headerData: List<String>) {
      sheet.createRow(0).let{
          headerData.asSequence().forEachIndexed { index, data ->
              it.createCell(index).setCellValue(data)
          }
      }
    }

    private fun setDataForSheet(sheet: Sheet, salesEvents: List<SalesEvent>) {
        salesEvents.asSequence().forEachIndexed { index, event ->
            sheet.createRow(index.inc()).apply { setCellValues(this, event) }
        }
    }

    private fun setCellValues(row: Row, event: SalesEvent) {
        event::class.memberProperties.asSequence().forEachIndexed { index, field ->
            field.isAccessible = true
            field.getter.call(event).let {
                if (it is MutableList<*> && it.size == 2)
                    row.createCell(index).setCellValue("${it[0]}, ${it[1]}")
                else row.createCell(index).setCellValue(it.toString())
            }
        }
    }
}