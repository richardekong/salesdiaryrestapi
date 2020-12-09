package com.daveace.salesdiaryrestapi.listeners

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.domain.SalesMetrics
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

interface RetentionMetrics {

    fun calculateAverageOrderValue(metrics: SalesMetrics): Double {
        val events = metrics.events
        if (metrics.category == SalesMetrics.Category.YEARLY.category && determineIntervals(events) == 365L)
            return metrics.totalProfit.div(events.size)
        return 0.0
    }

    fun calculateCustomerRetentionRate(metrics: SalesMetrics, customers: List<Customer>): Double {
        val endOfPeriod: LocalDate = determineEndOfPeriod(metrics)
        val startOfPeriod: LocalDate = determineBeginningOfPeriod(metrics)
        val numberOfCustomersAtEndOfPeriod: Double = customers.filter {
            it.date.isAfter(endOfPeriod.minusDays(7)) || it.date.isEqual(endOfPeriod)
        }.count().toDouble()
        val numberOfCustomersAtStartOfPeriod: Double = customers.filter {
            it.date.isEqual(startOfPeriod) || it.date.isBefore(startOfPeriod.plusDays(7))
        }.count().toDouble()
        val numberOfNewCustomers: Double = customers.filter {
            it.date.isAfter(startOfPeriod.plusDays(1)) && it.date.isBefore(endOfPeriod.plusDays(1))
        }.count().toDouble()

        return ((numberOfCustomersAtEndOfPeriod - numberOfNewCustomers) / numberOfCustomersAtStartOfPeriod) * 100

    }

    fun calculateCustomerChurnRate(customers: List<Customer>): Double {
        val beginningOfMonth: LocalDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        val endOfMonth: LocalDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
        val customersAtBeginningOfMonth: List<Customer> = customers.filter {
            it.date.isEqual(beginningOfMonth) || it.date.isBefore(beginningOfMonth.plusDays(8))
        }
        val customersAtEndOfMonth: List<Customer> = customers.filter {
            it.date.isAfter(endOfMonth.minusDays(8)) || it.date.isEqual(endOfMonth)
        }
        return (customersAtBeginningOfMonth.size - customersAtEndOfMonth.size).div(customersAtBeginningOfMonth.size.toDouble())

    }

    fun calculateLoyalCustomerRate(metrics: SalesMetrics, customers: List<Customer>): Double {
        return metrics.events.apply {
            map {
                occurrencesOfCustomers(it, this).toDouble()
            }.filter { freq -> freq > 4.0 }
        }.count().div(customers.distinctBy { it.id }.count().toDouble())
    }

    fun calculatePurchaseFrequency(metrics: SalesMetrics, customers: List<Customer>): Double {
        return metrics.events.size.div(
                customers.distinctBy { it.id }
                        .size.toDouble()
        )
    }

    fun calculateRepeatPurchaseProbabilities(metrics: SalesMetrics, customers: List<Customer>, purchases: Int): MutableList<Double> {
        val events = metrics.events
        val purchaseTimes = (1..purchases).toList().map { it.toDouble() }
        if (metrics.category == SalesMetrics.Category.YEARLY.category && determineIntervals(events) == 365L)
            return events
                    .map { event -> occurrencesOfCustomers(event, events).toDouble() }.sorted().run {
                        purchaseTimes
                                .map { numOfPurchases ->
                                    filter { it == numOfPurchases }.count().toDouble()
                                }
                                .map { it.div(customers.size) }
                    }
                    .toMutableList()
        return MutableList(customers.size) { it * 0.0 }

    }

    fun calculateRepeatPurchaseRate(metrics: SalesMetrics, customers: List<Customer>): Double {
        return metrics.events.run {
            map { occurrencesOfCustomers(it, this).toDouble() }
                    .filter { freq -> freq > 1.0 }
                    .count().div(customers.size.toDouble())
        }

    }

    private fun determineEndOfPeriod(metrics: SalesMetrics): LocalDate {
        val period: LocalDate = LocalDate.now()
        if (metrics.category == SalesMetrics.Category.DAILY.category)
            return period.with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay())
        if (metrics.category == SalesMetrics.Category.WEEKLY.category)
            return period.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        if (metrics.category == SalesMetrics.Category.MONTHLY.category)
            return period.with(TemporalAdjusters.lastDayOfMonth())
        if (metrics.category == SalesMetrics.Category.QUARTER.category)
            return period.with(TemporalAdjusters.firstDayOfMonth())
                    .plusMonths(2L)
                    .with(TemporalAdjusters.lastDayOfMonth())
        if (metrics.category == SalesMetrics.Category.SEMESTER.category)
            return period.with(TemporalAdjusters.firstDayOfMonth())
                    .plusMonths(5L)
                    .with(TemporalAdjusters.lastDayOfMonth())
        if (metrics.category == SalesMetrics.Category.YEARLY.category)
            return period.with(TemporalAdjusters.lastDayOfYear())

        return metrics.events.run { last().date }
    }

    private fun determineBeginningOfPeriod(metrics: SalesMetrics): LocalDate {
        val period: LocalDate = LocalDate.now()
        if (metrics.category == SalesMetrics.Category.DAILY.category)
            return period.with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay())
        if (metrics.category == SalesMetrics.Category.WEEKLY.category)
            return period.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        if (metrics.category == SalesMetrics.Category.MONTHLY.category)
            return period.with(TemporalAdjusters.firstDayOfMonth())
        if (metrics.category == SalesMetrics.Category.QUARTER.category)
            return period.with(TemporalAdjusters.firstDayOfMonth())
                    .plusMonths(2L)
                    .with(TemporalAdjusters.firstDayOfMonth())
        if (metrics.category == SalesMetrics.Category.SEMESTER.category)
            return period.with(TemporalAdjusters.firstDayOfMonth())
                    .plusMonths(5L)
        if (metrics.category == SalesMetrics.Category.YEARLY.category)
            return period.with(TemporalAdjusters.firstDayOfYear())
        return metrics.events.first().date

    }

    private fun occurrencesOfCustomers(event: SalesEvent, events: MutableList<SalesEvent>): Int = events.filter { event.customerId == it.customerId }.count()

    private fun determineIntervals(events: MutableList<SalesEvent>): Long = events.map { it.date }.sorted().run { first().until(last(), ChronoUnit.DAYS) }
}