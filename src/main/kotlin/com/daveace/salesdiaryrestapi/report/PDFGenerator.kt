package com.daveace.salesdiaryrestapi.report

import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class PDFGenerator {
    companion object {

        fun <T : Any> generatePDF(report: Flux<T>): Mono<ByteArrayInputStream> {
            return report.collectList().map { data ->
                toPDF(data)
            }.onErrorMap { error ->
                throw RuntimeException(error.message)
            }
        }

        private fun <T : Any> toPDF(data: MutableList<T>): ByteArrayInputStream {
            val doc = Document()
            val outputStream = ByteArrayOutputStream()
            try {
                val headerLabels: List<String> = createHeader(data[0]::class)
                val table: PdfPTable = createPDFTable(headerLabels.size)
                table.setWidths(listOf(3,3,2,2,2,3,3,3,3,3,3).toIntArray())
                addHeaderToPDFTable(headerLabels, table)
                addDataToPDFTable(table, data)
                PdfWriter.getInstance(doc, outputStream)
                doc.open()
                addEmptyLines(doc, 2)
                doc.add(table)
                addEmptyLines(doc, 3)
                doc.close()
            } catch (ex: Exception) {
                throw RuntimeException(ex.message)
            }
            return ByteArrayInputStream(outputStream.toByteArray())
        }

        private fun createPDFTable(columns: Int): PdfPTable {
            return PdfPTable(columns)
        }

        private fun <T : Any> createHeader(kClass: KClass<T>): List<String> {
            return kClass.memberProperties.asSequence().map { field ->
                field.isAccessible = true
                val label: String = field.name.toUpperCase()
                field.isAccessible = false
                label
            }.toList()
        }

        private fun addHeaderToPDFTable(headerLabels: List<String>, table: PdfPTable) {
            headerLabels.asSequence().forEach { label ->
                val cell: PdfPCell = PdfPCell(Phrase(label)).apply {
                    horizontalAlignment = Element.ALIGN_CENTER
                }
                table.addCell(cell)
                table.headerRows = 1
            }
        }

        private fun <T : Any> addDataToPDFTable(table: PdfPTable, data: MutableList<T>) {
            data.asSequence().forEach { row ->
                addDatumToCell(table, row)
            }
        }

        private fun <T : Any> addDatumToCell(table: PdfPTable, datum: T) {
            datum::class.memberProperties.asSequence()
                    .forEach {
                        it.isAccessible = true
                        var value:String = it.getter.call(datum).toString()
                        if (it.name.contains("id", true))
                            value = value.substring(0, 4)
                        val cell: PdfPCell = PdfPCell(Phrase(value))
                            .apply {
                                    horizontalAlignment = Element.ALIGN_CENTER
                                    verticalAlignment = Element.ALIGN_MIDDLE
                                }
                        table.addCell(cell)
                        it.isAccessible = false
                    }
        }

        private fun addEmptyLines(doc: Document, lines: Int) {
            repeat(IntRange(0, lines).count()) {
                doc.add(Paragraph("\u2000"))
            }
        }
    }
}