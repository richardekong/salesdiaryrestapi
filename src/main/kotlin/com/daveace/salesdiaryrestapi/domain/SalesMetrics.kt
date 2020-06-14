package com.daveace.salesdiaryrestapi.domain

import com.fasterxml.jackson.annotation.JsonIgnore

data class SalesMetrics(
        var category: String = "",
        @JsonIgnore var events: MutableList<SalesEvent> = mutableListOf(),
        var totalCost: Double = 0.0,
        var totalSales: Double = 0.0,
        var totalProfit: Double = 0.0) {

    enum class Category(val category: String) {
        REGULAR("Regular"),
        PERIODIC("Periodic"),
        DAILY("Daily"), WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        QUARTER("Quarter"),
        SEMESTER("Semester"),
        YEARLY("Yearly")
    }

    constructor(events: MutableList<SalesEvent>) : this() {
        this.events = events
        this.totalCost = getTotalCost(events)
        this.totalSales = getTotalSales(events)
        this.totalProfit = getTotalProfit(getProfits(events))
    }

    constructor(category: String, events: MutableList<SalesEvent>) : this(events) {
        this.category = category
    }

    private fun getTotalCost(events: MutableList<SalesEvent>): Double {
        return events.asSequence().sumByDouble { it.costPrice * it.quantitySold }
    }

    private fun getTotalSales(events: MutableList<SalesEvent>): Double {
        return events.asSequence().sumByDouble { it.salesPrice * it.quantitySold }
    }

    private fun getProfits(events: MutableList<SalesEvent>): MutableList<Double> {
        return events.asSequence().map { getProfit(it) }.toMutableList()
    }

    private fun getTotalProfit(profits: MutableList<Double>): Double {
        return profits.asSequence().sumByDouble { it }
    }

    private fun getProfit(event: SalesEvent) = event.quantitySold * (event.salesPrice - event.costPrice)
}
