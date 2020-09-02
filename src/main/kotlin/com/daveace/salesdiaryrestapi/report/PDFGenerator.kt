package com.daveace.salesdiaryrestapi.report

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

class PDFGenerator {
    companion object{

        fun<T:Any> generatePDF(report:Flux<T>): Mono<ByteArrayInputStream> {
            TODO("Not yet implemented")
        }
    }
}