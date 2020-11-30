package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import java.time.LocalDate

interface ReactiveSalesEventRepository: ReactiveMongoRepository<SalesEvent, String> {
    fun findSalesEventsByDate(date: LocalDate): Flux<SalesEvent>
}