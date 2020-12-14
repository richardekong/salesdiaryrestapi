package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Credit
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.repository.ReactiveCreditRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ReactiveCreditServiceImpl:ReactiveCreditService {

    private lateinit var repo: ReactiveCreditRepository

    @Autowired
    fun initRepo(repo:ReactiveCreditRepository){
        this.repo = repo
    }

    override fun createCreditRecord(credit: Credit): Mono<Credit> {
        return repo.save(credit)
    }

    override fun createCreditRecord(event: SalesEvent): Mono<Credit> {
        return repo.save(Credit(event))
    }

    override fun findCreditById(id: String): Mono<Credit> {
        return repo.findById(id)
    }

    override fun findCreditsByCustomerId(id: String): Flux<Credit> {
        return repo.findByCustomerId(id)
    }

    override fun findCreditsByProductId(id: String): Flux<Credit> {
        return repo.findByProductId(id)
    }

    override fun findAllCredits(): Flux<Credit> {
        return repo.findAll()
    }
}