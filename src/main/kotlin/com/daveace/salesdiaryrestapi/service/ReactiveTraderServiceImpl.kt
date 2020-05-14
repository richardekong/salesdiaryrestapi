package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ReactiveTraderServiceImpl : ReactiveTraderService{

    @Autowired
    private lateinit var traderRepo:ReactiveTraderRepository

    override fun findTrader(email: String): Mono<Trader> {
        return traderRepo.findById(email)
    }

    override fun findAllTraders(): Flux<Trader> {
        return traderRepo.findAll()
    }

}

