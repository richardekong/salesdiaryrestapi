package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Expenditure
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveExpenditureService {

    fun createExpenditure(traderId:String,entries:MutableMap<String,Double>): Mono<Expenditure>
    fun findExpenditureById(id:String):Mono<Expenditure>
    fun findExpenditureByTraderId(id:String):Mono<Expenditure>
    fun findExpenditures(): Flux<Expenditure>
    fun findExpenditures(date:String):Flux<Expenditure>
    fun findExpenditures(start:String, end:String):Flux<Expenditure>
//    fun editExpenditureByDate(date:String, desc:String, expense: Expenditure.Expense):Mono<Expenditure>
    fun editExpenditureById(id:String, desc: String, expense: Expenditure.Expense):Mono<Expenditure>
    fun deleteExpenditureById(id:String):Mono<Void>
}