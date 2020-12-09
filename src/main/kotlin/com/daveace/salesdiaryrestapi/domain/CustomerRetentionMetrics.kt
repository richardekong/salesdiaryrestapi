package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.listeners.RetentionMetrics
import com.daveace.salesdiaryrestapi.mapper.Mappable

class CustomerRetentionMetrics() : Mappable, RetentionMetrics {

    private var averageOrderValue: Double = 0.0
    private var customerRetentionRate: Double = 0.0
    private var customerChurnRate: Double = 0.0
    private var loyalCustomerRate: Double = 0.0
    private var purchaseFrequency: Double = 0.0
    private var repeatPurchaseProbabilities: MutableList<Double> = mutableListOf()
    private var repeatPurchaseRate: Double = 0.0

    constructor(metrics: SalesMetrics, customers: List<Customer>, purchases: Int = customers.size) : this() {
        averageOrderValue = calculateAverageOrderValue(metrics)
        customerRetentionRate = calculateCustomerRetentionRate(metrics, customers)
        customerChurnRate = calculateCustomerChurnRate(customers)
        loyalCustomerRate = calculateLoyalCustomerRate(metrics, customers)
        purchaseFrequency = calculatePurchaseFrequency(metrics, customers)
        repeatPurchaseProbabilities = calculateRepeatPurchaseProbabilities(metrics, customers, purchases)
        repeatPurchaseRate = calculateRepeatPurchaseRate(metrics, customers)
    }

}

