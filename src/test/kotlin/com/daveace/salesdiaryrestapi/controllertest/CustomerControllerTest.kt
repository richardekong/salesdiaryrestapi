//package com.daveace.salesdiaryrestapi.controllertest
//
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMER
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMERS
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.populateReactiveRepository
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldDeleteEntity
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPatchEntity
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPostEntity
//import com.daveace.salesdiaryrestapi.domain.Customer
//import com.daveace.salesdiaryrestapi.repository.ReactiveCustomerRepository
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.junit.jupiter.api.extension.ExtendWith
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import org.springframework.test.web.reactive.server.WebTestClient
//import java.util.*
//
//@ExtendWith(SpringExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class CustomerControllerTest {
//
//    @MockBean
//    private lateinit var customerRepo: ReactiveCustomerRepository
//    private lateinit var testClient: WebTestClient
//
//    private fun createTestCustomer():Customer {
//        val email = UUID.randomUUID().toString().substring(0,3).plus("@mail.com")
//        return Customer(
//                "Reilly",
//                email,
//                UUID.randomUUID().toString(),
//                "Black Net",
//                "c/fake_signaturePath",
//                "No 45 Kelly Road"
//                )
//    }
//
//    private fun createTestCustomers():Array<Customer> {
//        return arrayOf(
//                Customer("Benny", "Benny@mail.net",UUID.randomUUID().toString(), "Rhino", "c:/fake_signaturePath", "No 35 doodle park"),
//                Customer("Barry","barry@mail.net", UUID.randomUUID().toString(), "Barristock", "c:/fake_signaturePath","No 12 dums avenue"),
//                Customer("Kelly", "kelly@mail.net",UUID.randomUUID().toString(),"Kelly Stock", "c:/fake_signaturePath","No 32 stock park")
//        )
//    }
//
//    @BeforeAll
//    fun populateCustomerDb(){
//        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
//        populateReactiveRepository(customerRepo, createTestCustomers().toList())
//    }
//
//    @Test
//    fun shouldSaveACustomer(){
//        val endpoint = "$API$SALES_DIARY_CUSTOMERS"
//        val customer = createTestCustomer()
//        shouldPostEntity(customer, customerRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo(customer.name)
//                .jsonPath("$.email").isEqualTo(customer.email)
//                .jsonPath("$.traderId").isEqualTo(customer.traderId)
//                .jsonPath("$.company").isEqualTo(customer.company)
//                .jsonPath("$.location").isEqualTo(customer.location)
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//    }
//
//    @Test
//    fun shouldFindACustomer(){
//        val customer = createTestCustomer()
//        val endpoint = "$API$SALES_DIARY_CUSTOMER${customer.email}"
//        shouldGetEntity(customer.email, customer ,customerRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$").isNotEmpty
//                .jsonPath("$.email").exists()
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//    }
//
//    @Test
//    fun shouldFindCustomers(){
//        val endpoint = "$API$SALES_DIARY_CUSTOMERS"
//        val customerFlux = customerRepo.findAll()
//        shouldGetEntities(customerFlux, customerRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$").isArray
//                .jsonPath("$").isNotEmpty
//                .jsonPath("$..links").isArray
//                .jsonPath("$..links[0].rel").exists()
//                .jsonPath("$..links[0].href").exists()
//    }
//
//    @Test
//    fun shouldUpdateACustomer(){
//        val oldCustomer = createTestCustomer()
//        val newCustomer = oldCustomer.copy()
//        newCustomer.company = "Broad Way corporation"
//        shouldPatchEntity(oldCustomer, newCustomer,customerRepo, testClient, "$SALES_DIARY_CUSTOMERS?email=${oldCustomer.email}")
//    }
//
//    @Test
//    fun shouldDeleteACustomer(){
//    val customer = createTestCustomer()
//        shouldDeleteEntity(customer.email, testClient, customerRepo, "$SALES_DIARY_CUSTOMER${customer.email}")
//    }
//
//}