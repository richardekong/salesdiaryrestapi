package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.SalesMetrics
import org.springframework.hateoas.RepresentationModel

class SalesMetricsModel(): RepresentationModel<SalesMetricsModel>(){

    lateinit var category: String
    lateinit var totalCost:Number
    lateinit var totalSales:Number
    lateinit var totalProfit:Number

    constructor(metrics: SalesMetrics):this(){
        this.category = metrics.category
        this.totalCost = metrics.totalCost
        this.totalSales = metrics.totalSales
        this.totalProfit = metrics.totalProfit
    }

}