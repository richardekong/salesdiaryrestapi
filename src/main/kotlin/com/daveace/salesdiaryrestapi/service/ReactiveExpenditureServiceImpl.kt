package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Expenditure
import com.daveace.salesdiaryrestapi.repository.ReactiveExpenditureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ReactiveExpenditureServiceImpl : ReactiveExpenditureService {

    companion object {
        private const val DATE_PATTERN = "yyyy-MM-dd"
    }

    private lateinit var repo: ReactiveExpenditureRepository

    @Autowired
    private fun initRepo(repo: ReactiveExpenditureRepository) {
        this.repo = repo
    }

    override fun createExpenditure(traderId: String, entries: MutableMap<String, Double>): Mono<Expenditure> {
        return repo.save(Expenditure(entries, traderId))
    }

    override fun findExpenditureById(id: String): Mono<Expenditure> {
        return repo.findById(id)
    }

    override fun findExpenditureByTraderId(id: String): Mono<Expenditure> {
        return repo.findExpenditureByTraderId(id)
    }

    override fun findExpenditures(): Flux<Expenditure> {
        return repo.findAll()
    }

    override fun findExpenditures(date: String): Flux<Expenditure> {
        return findExpenditures().filter { it.date() == LocalDate.parse(date) }
    }

    override fun findExpenditures(start: String, end: String): Flux<Expenditure> {
        val startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern(DATE_PATTERN))
        val endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern(DATE_PATTERN))
        return findExpenditures().filter { it.date().isAfter(startDate) && it.date().isBefore(endDate) }
    }

    override fun editExpenditureById(id: String, desc: String, expense: Expenditure.Expense): Mono<Expenditure> {
        return findExpenditureById(id)
            .flatMap { expenditure ->
                expenditure.expenses().find { it.description == desc }
                    ?.apply {
                        description = expense.description
                        amount = expense.amount
                    }
                repo.save(expenditure)
            }
    }

//    override fun editExpenditureByDate(date: String, desc: String, expense: Expenditure.Expense): Mono<Expenditure> {
//        return findExpenditures()
//            .filter { expenditure ->
//                expenditure.date == LocalDate.parse(
//                    date,
//                    DateTimeFormatter.ofPattern(DATE_PATTERN)
//                )
//            }.flatMap { expenditure ->
//                expenditure.expenses().find { it.description == desc }
//                    ?.apply {
//                        this.description = expense.description
//                        this.amount = expense.amount
//                    }
//                repo.save(expenditure)
//            }.single()
//    }

    override fun deleteExpenditureById(id: String): Mono<Void> {
        return repo.deleteExpenditureById(id)
    }
}