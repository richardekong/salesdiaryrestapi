package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.BaseTests
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENT_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENT_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENT_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENT_METRICS
import com.daveace.salesdiaryrestapi.repository.ReactiveSalesEventRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

class SalesEventControllerTest: BaseTests() {

    @MockBean
    private lateinit var eventRepo: ReactiveSalesEventRepository
    private lateinit var testClient: WebTestClient

    @Test
    @Order(1)
    fun shouldPostSalesEvent(){
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS"
    }

    @Test
    @Order(2)
    fun shouldGetSalesEvents(){
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS"
    }

    @Test
    @Order(3)
    fun shouldGetSalesEventsByDate(){
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS?date=value"
    }

    @Test
    @Order(4)
    fun shouldGetDailySalesEvents(){
        val endpoint = "$API$SALES_DIARY_DAILY_SALES_EVENTS"
    }

    @Test
    @Order(5)
    fun shouldGetWeeklySalesEvents(){
        val endpoint = "$API$SALES_DIARY_WEEKLY_SALES_EVENTS"
    }

    @Test
    @Order(6)
    fun shouldGetMonthlySalesEvents(){
        val endpoint = "$API$SALES_DIARY_MONTHLY_SALES_EVENTS"
    }

    @Test
    @Order(7)
    fun shouldGetQuarterlySalesEvent(){
        val endpoint = "$API$SALES_DIARY_QUARTERLY_SALES_EVENTS"
    }

    @Test
    @Order(8)
    fun shouldGetSemesterSalesEvents(){
        val endpoint = "$API$SALES_DIARY_SEMESTER_SALES_EVENTS"
    }

    @Test
    @Order(9)
    fun shouldGetYearlySalesEvents(){
        val endpoint = "$API$SALES_DIARY_YEARLY_SALES_EVENTS"
    }

    @Test
    @Order(10)
    fun shouldGetSalesEventsByDateRange(){
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS?from=startDate&to=endDate=value"
    }

    @Test
    @Order(11)
    fun shouldGetSalesEvent(){
        val endpoint = "$API$SALES_DIARY_SALES_EVENT{id}"
    }

    @Test
    @Order(12)
    fun shouldGetSalesEventMetricsByDate(){
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS/metrics"
    }

    @Test
    @Order(13)
    fun shouldGetDailySalesEventsMetrics(){
        val endpoint = "$API$SALES_DIARY_DAILY_SALES_EVENTS_METRICS"
    }

    @Test
    @Order(14)
    fun shouldGetWeeklySalesEventsMetrics(){
        val endpoint = "$API$SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS"
    }

    @Test
    @Order(15)
    fun shouldGetMonthlySalesEventsMetrics(){
        val endpoint = "$API$SALES_DIARY_MONTHLY_SALES_EVENT_METRICS"
    }

    @Test
    @Order(16)
    fun shouldGetQuarterlySalesEventsMetrics(){
        val endpoint = "$API$SALES_DIARY_QUARTERLY_SALES_EVENT_METRICS"
    }

    @Test
    @Order(17)
    fun shouldGetSemesterSalesEventsMetrics(){
        val endpoint = "$API$SALES_DIARY_SEMESTER_SALES_EVENT_METRICS"
    }

    @Test
    @Order(18)
    fun shouldGetYearlySalesEventsMetrics(){
        val endpoint = "$API$SALES_DIARY_YEARLY_SALES_EVENT_METRICS"
    }

    @Test
    @Order(19)
    fun shouldGetSalesEventsMetricsByDateRange(){
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS?from=startDate&to=endDate/metrics"
    }

}

