package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Expenditure
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDate

@Relation(value = "expenditure", collectionRelation = "expenditures")
class ExpenditureModel() : RepresentationModel<ExpenditureModel>() {

    var traderId: String = ""
    var date: LocalDate = LocalDate.now()
    private var expenses: MutableList<Expenditure.Expense> = mutableListOf()
    var total: Double = 0.0

    constructor(expenditure: Expenditure) : this() {
        this.traderId = expenditure.traderId
        this.date = expenditure.date
        this.expenses = expenditure.expenses()
        this.total = expenditure.total()
    }
}