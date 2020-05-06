package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ReactiveSalesEventRepository: ReactiveMongoRepository<SalesEvent, String> {
}