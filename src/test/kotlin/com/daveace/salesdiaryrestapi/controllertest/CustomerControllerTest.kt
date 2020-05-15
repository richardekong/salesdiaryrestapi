package com.daveace.salesdiaryrestapi.controllertest

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMERS
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.performLoginOperation
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.performSignUpOperation
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.repository.ReactiveCustomerRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerControllerTest {

    @MockBean
    private lateinit var customerRepo: ReactiveCustomerRepository
    private lateinit var testClient: WebTestClient
    private lateinit var testCustomer: Customer
    private lateinit var testUser: User
    private var authorizationToken: String = ""

    private fun createTestCustomer(): Customer {
        val email = UUID.randomUUID().toString().substring(0, 3).plus("@mail.com")
        return Customer(
                "Reilly",
                email,
                UUID.randomUUID().toString(),
                "Black Net",
                "c/fake_signaturePath",
                "No 45 Kelly Road"
        )
    }

    private fun createTestUser(customer: Customer): User {
        return User(customer.email, "test123", "093473467342")
    }

    @BeforeAll
    fun init() {
        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
        testCustomer = createTestCustomer()
        testUser = createTestUser(testCustomer)
        performSignUpOperation(testClient, testUser)
    }

    @BeforeEach
    fun login() {
        performLoginOperation(testClient, testUser.email, testUser.password)
                .value<String> { token ->
                    if (authorizationToken.isEmpty()) authorizationToken = token
                }
    }

    @Test
    @Order(1)
    fun shouldFindACustomer() {
        val endpoint = "$API$SALES_DIARY_CUSTOMER${testCustomer.email}"
        shouldGetEntity(testCustomer.email, testCustomer, customerRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isNotEmpty
                .jsonPath("$..email").exists()
                .jsonPath("$.._links.self.href").exists()
                .jsonPath("$.._links.self.href").isNotEmpty
    }

    @Test
    fun shouldFindAllCustomers() {
        val endpoint = "$API$SALES_DIARY_CUSTOMERS"
        shouldGetEntities(customerRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isArray
                .jsonPath("$").isNotEmpty
                .jsonPath("$..links").isArray
                .jsonPath("$..links[0].rel").exists()
                .jsonPath("$..links[0].href").exists()
    }


}