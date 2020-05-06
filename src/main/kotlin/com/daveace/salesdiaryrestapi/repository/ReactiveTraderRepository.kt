package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.Trader
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ReactiveTraderRepository: ReactiveMongoRepository<Trader, String> {
}