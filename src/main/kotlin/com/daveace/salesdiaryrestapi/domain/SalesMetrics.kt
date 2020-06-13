package com.daveace.salesdiaryrestapi.domain

import com.fasterxml.jackson.annotation.JsonIgnore

data class SalesMetrics(var category: String = "", @JsonIgnore val events: MutableList<SalesEvent>) {

    val totalCost: Double = getTotalCost(events)
    val totalSales: Double = getTotalSales(events)
    val totalProfit: Double = getTotalProfit(getProfits(events))

    private fun getTotalCost(events: MutableList<SalesEvent>): Double {
        return events.asSequence().sumByDouble { it.costPrice * it.quantitySold }
    }

    private fun getTotalSales(events: MutableList<SalesEvent>): Double {
        return events.asSequence().sumByDouble { it.salesPrice * it.quantitySold}
    }

    private fun getProfits(events: MutableList<SalesEvent>): MutableList<Double> {
        return events.asSequence().map { getProfit(it) }.toMutableList()
    }

    private fun getTotalProfit(profits: MutableList<Double>): Double {
        return profits.asSequence().sumByDouble { it }
    }

    private fun getProfit(event: SalesEvent) = event.quantitySold * (event.salesPrice - event.costPrice)
}
