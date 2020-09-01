package com.daveace.salesdiaryrestapi.report

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class ExcelGenerator {

    companion object {
        fun <T : Any> generateExcel(report: Flux<T>): Mono<ByteArrayInputStream> {
            return report.collectList().map { data ->
                toExcel(data)
            }.onErrorMap { error ->
                throw RuntimeException(error.message)
            }
        }

        private fun <T : Any> toExcel(data: MutableList<T>): ByteArrayInputStream {
            return ByteArrayInputStream(
                    ByteArrayOutputStream().apply {
                        val workbook: Workbook = XSSFWorkbook()
                        val sheet: Sheet = workbook.createSheet()
                        addHeaderToSheet(sheet, createHeaderForSheet(data[0]::class))
                        addDataToSheet(sheet, data)
                        try {
                            workbook.write(this)
                        } catch (ioe: IOException) {
                            throw RuntimeException(ioe.message)
                        } finally {
                            workbook.close()
                        }
                    }
                            .toByteArray()
            )
        }

        private fun addHeaderToSheet(sheet: Sheet, header: List<String>) {
            sheet.createRow(0).let { row ->
                header.asSequence().forEachIndexed { index, headerData ->
                    row.createCell(index).setCellValue(headerData)
                }
            }
        }

        private fun <T : Any> createHeaderForSheet(kClass: KClass<T>): List<String> {
            return kClass.memberProperties.asIterable().map {
                it.isAccessible = true
                val word: String = it.name.toUpperCase()
                it.isAccessible = false
                word
            }
        }

        private fun <T : Any> addDataToSheet(sheet: Sheet, data: List<T>) {
            data.asSequence().forEachIndexed { index, datum ->
                sheet.createRow(index.inc()).apply { addValueToCell(this, datum) }
            }
        }

        private fun <T : Any> addValueToCell(row: Row, datum: T) {
            datum::class.memberProperties.asSequence().forEachIndexed { index, field ->
                field.isAccessible = true
                field.getter.call(datum).let { value ->
                    if (datum is SalesEvent && value is MutableList<*> && value.size == 2)
                        row.createCell(index).setCellValue("${value[0]}, ${value[1]}")
                    else row.createCell(index).setCellValue(value.toString())
                }
                field.isAccessible = false
            }
        }
    }
}