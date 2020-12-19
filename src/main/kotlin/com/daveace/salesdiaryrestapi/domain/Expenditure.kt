package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.mapper.Mappable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Document
data class Expenditure(@field:Id val id: String = SalesDiaryId.generateId()) : Mappable {
    var traderId: String = ""
    val date: LocalDate = LocalDate.now()
    private val expenses: MutableList<Expense> = mutableListOf()
    private var total: Double = 0.0

    constructor(
        @NotNull @Size(min = 1, message = "Entries must contain one entry at least")
        entries: MutableMap<
                @NotBlank(message = "Description required") String,
                @DecimalMin("1.00", message = "Amount must be at least 1.00") Double>,
        traderId: String
    ) : this() {
        entries.forEach { entry -> expenses.add(Expense(entry.key, entry.value)) }
        total = expenses.sumByDouble { it.amount }
        this.traderId = traderId
    }

    fun expenses():MutableList<Expense> = expenses

    fun total():Double = total

    data class Expense(var description: String, var amount: Double) : Mappable
}