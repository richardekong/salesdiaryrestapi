package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.BaseTests
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.test.util.AssertionErrors.assertTrue


class SalesMetricsTests : BaseTests(){


    private lateinit var testSalesMetrics: SalesMetrics
    companion object {
        private const val errorMessage: String = "Expected %.2f to be %.2f"
    }
    private fun createTestSalesEvents(): MutableList<SalesEvent> {
        return mutableListOf(
                SalesEvent("TID001", "CID001", "PID001", 10.0, 150.00,
                        155.00, 3.00, mutableListOf(12.934, 31.034)),
                SalesEvent("TID002", "CID002", "PID002", 12.0, 100.00,
                        110.00, 6.00, mutableListOf(12.934, 31.034)),
                SalesEvent("TID003", "CID002", "PID003", 6.0, 30.00,
                        45.00, 3.00, mutableListOf(12.934, 31.034))
        )
    }

    @BeforeAll
    fun init() {
        testSalesMetrics = SalesMetrics(createTestSalesEvents())
    }

    @Test
    @Order(1)
    fun shouldGetTotalCost() {
        val expectedValue = createTestSalesEvents()
                .asSequence()
                .sumByDouble {
                    it.costPrice * it.quantitySold
                }
        val testResult: Double = testSalesMetrics.totalCost
        assertTrue(String.format(errorMessage,testResult, expectedValue), expectedValue == testResult)
    }

    @Test
    @Order(2)
    fun shouldGetTotalSales() {
        val expectedValue: Double = createTestSalesEvents()
                .asSequence()
                .sumByDouble {
                    it.salesPrice * it.quantitySold
                }
        val testResult: Double = testSalesMetrics.totalSales
        assertTrue(String.format(errorMessage,testResult, expectedValue), expectedValue == testResult)
    }

    @Test
    @Order(3)
    fun shouldGetTotalProfit() {
        val expectedValue: Double = createTestSalesEvents()
                .asSequence().sumByDouble { it.quantitySold * (it.salesPrice - it.costPrice) }
        val testResult: Double = testSalesMetrics.totalProfit
        assertTrue(String.format(errorMessage,testResult, expectedValue), expectedValue == testResult)
    }


}