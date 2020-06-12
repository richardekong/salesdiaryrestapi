package com.daveace.salesdiaryrestapi.controller.test

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADERS
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.APPLICATION_JSON
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.AUTHORIZATION
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.PREFIX
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.createWebTestClient
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.performDeleteOperation
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.performLoginOperation
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.performSignUpOperation
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.shouldGetEntities
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.shouldGetEntity
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.shouldGetResponse
import com.daveace.salesdiaryrestapi.controller.test.ControllerTestFactory.Companion.shouldPostEntity
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.hateoas.model.CustomerModel
import com.daveace.salesdiaryrestapi.hateoas.model.ProductModel
import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
import com.daveace.salesdiaryrestapi.service.ReactiveTraderServiceImpl
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.hateoas.Link
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.AssertionErrors.assertTrue
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.properties"])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraderControllerTest {

    @MockBean
    private lateinit var traderRepo: ReactiveTraderRepository

    @MockBean
    private lateinit var traderService: ReactiveTraderServiceImpl
    private lateinit var testClient: WebTestClient
    private lateinit var testUser: User
    private lateinit var testTrader: Trader
    private lateinit var testProduct: Product
    private lateinit var testCustomer: Customer
    private var authorizationToken: String = ""

    @BeforeAll
    fun init() {
        testClient = createWebTestClient()
        testUser = createTestUser()
        testTrader = createTestTrader(testUser)
        testUser.trader = testTrader
        testProduct = createTestProduct(testTrader)
        testCustomer = createTestCustomer(testTrader)
        performSignUpOperation(testClient, testUser)
    }

    @BeforeEach
    fun login() {
        performLoginOperation(testClient, testUser.email, testUser.password)
                .value<String> {
                    if (authorizationToken.isEmpty()) {
                        authorizationToken = it
                    }
                }
    }

    @AfterAll
    fun deleteUser() {
        performDeleteOperation(testClient, testUser.email, authorizationToken)
    }

    private fun createTestUser(): User {
        val randomEmail: String = UUID.randomUUID().toString().substring(0, 3).plus("@mail.com")
        return User(randomEmail, "test123")
    }

    private fun createTestTrader(user: User): Trader {
        return Trader(user.id, user.email, "Mickey", "09034734632", "test address")
    }

    private fun createTestProduct(trader: Trader): Product {
        return Product(trader.id, "Product001", "code0019238", "img/path/product001", 10.00, 100.00)
    }

    private fun createTestCustomer(trader: Trader): Customer {
        val email = UUID.randomUUID().toString().substring(0, 3).plus("@mail.com")
        val location: MutableList<Double> = mutableListOf(12.00, 13.00)
        return Customer(email, "Customer001", trader.id, "company001", "address001", location)
    }

    @Test
    @Order(1)
    fun shouldFindAllTrader() {
        val endpoint = "$API$SALES_DIARY_TRADERS"
        shouldGetEntities(traderRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isArray
                .jsonPath("$").isNotEmpty
                .jsonPath("$..links").exists()
                .jsonPath("$..links").isArray
                .jsonPath("$..links[0].rel").exists()
                .jsonPath("$..links[0].href").exists()

    }

    @Test
    @Order(2)
    fun shouldFindATrader() {
        val traderEmail: String = testTrader.email
        val endpoint = "$API$SALES_DIARY_TRADER$traderEmail"
        shouldGetEntity(testTrader.email, testTrader, traderRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isMap
                .jsonPath("$.email").isEqualTo(testTrader.email)
                .jsonPath("$.name").isEqualTo(testTrader.name)
                .jsonPath("$.address").isEqualTo(testTrader.address)

    }

    @Test
    @Order(3)
    fun shouldAddAProduct() {
        val email = testTrader.email
        val endpoint = "$API$SALES_DIARY_TRADER$email/products"
        val monoProduct: Mono<Product> = Mono.just(testProduct)
        val model = ProductModel(testProduct)
        Mockito.`when`(traderService.addProduct(email, testProduct)).thenReturn(monoProduct)
        shouldPostEntity(model, monoProduct, testClient, endpoint, authorizationToken)
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(testProduct.id)
                .jsonPath("$.traderId").isEqualTo(testProduct.traderId)
                .jsonPath("$.name").isEqualTo(testProduct.name)
                .jsonPath("$.code").isEqualTo(testProduct.code)
                .jsonPath("$.imagePath").isEqualTo(testProduct.imagePath)
                .jsonPath("$.stock").isEqualTo(testProduct.stock)
                .jsonPath("$.cost").isEqualTo(testProduct.cost)
    }

    @Test
    @Order(4)
    fun shouldAddACustomer() {
        val email = testTrader.email
        val endpoint = "$API$SALES_DIARY_TRADER$email/customers"
        val monoCustomer: Mono<Customer> = Mono.just(testCustomer)
        val model = CustomerModel(testCustomer)
        Mockito.`when`(traderService.addCustomer(email, testCustomer)).thenReturn(Mono.just(testCustomer))
        shouldPostEntity(model, monoCustomer, testClient, endpoint, authorizationToken)
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(testCustomer.id)
                .jsonPath("$.name").isEqualTo(testCustomer.name)
                .jsonPath("$.email").isEqualTo(testCustomer.email)
                .jsonPath("$.traderId").value<String> {
                    assertTrue("expect ${testCustomer.traderId} to equals $it",
                            it == testCustomer.traderId
                    )
                    assertTrue("expect ${testTrader.id} to equals $it",
                            testTrader.id == it
                    )
                }
                .jsonPath("$.company").isEqualTo(testCustomer.company)
                .jsonPath("$.signaturePath").isEqualTo(testCustomer.signaturePath)
                .jsonPath("$.address").isEqualTo(testCustomer.address)
                .jsonPath("$.location").isEqualTo(testCustomer.location)

    }

    @Test
    @Order(5)
    fun shouldFindATradersProduct() {
        val traderId: String = testTrader.id
        val productId: String = testProduct.id
        val endpoint = "$API$SALES_DIARY_TRADER$traderId/products/$productId"
        val monoProduct: Mono<Product> = Mono.just(testProduct)
        Mockito.`when`(traderService.findTraderProduct(traderId, productId)).thenReturn(monoProduct)
        shouldGetResponse(testClient, endpoint, authorizationToken)
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(testProduct.id)
                .jsonPath("$.name").isEqualTo(testProduct.name)
                .jsonPath("$.traderId").isEqualTo(testProduct.traderId)
                .jsonPath("$.code").isEqualTo(testProduct.code)
                .jsonPath("$.imagePath").isEqualTo(testProduct.imagePath)
                .jsonPath("$.stock").isEqualTo(testProduct.stock)
                .jsonPath("$.cost").isEqualTo(testProduct.cost)
                .jsonPath("$.traderId").value<String> {
                    assertTrue("Expect ${testProduct.traderId} to equals $it",
                            testProduct.traderId == it)
                }

    }

    @Test
    @Order(6)
    fun shouldFindATradersProducts() {
        val traderId: String = testTrader.id
        val endpoint = "$API$SALES_DIARY_TRADER$traderId/products"
        val productFlux: Flux<Product> = traderService.findTraderProducts(traderId)
        Mockito.`when`(traderService.findTraderProducts(traderId)).thenReturn(productFlux)
        val responseBody: Flux<ProductModel> = shouldGetResponse(testClient, endpoint, authorizationToken)
                .expectStatus().isOk.returnResult(ProductModel::class.java).responseBody
        val href = "$BASE_URL$endpoint?size=1&page=0&sort=id&dir=asc"
        val links: MutableList<Link> = mutableListOf(
                Link.of(href, "first"),
                Link.of(href, "last"),
                Link.of(href, "self")
        )
        val expectedModel: ProductModel = ProductModel(testProduct).add(links)

        StepVerifier.create(responseBody)
                .expectNext(expectedModel)
                .verifyComplete()
    }

    @Test
    @Order(7)
    fun shouldFindATradersCustomer() {
        val traderId: String = testTrader.id
        val customerEmail: String = testCustomer.email
        val customerMono: Mono<Customer> = Mono.just(testCustomer)
        val endpoint = "$API$SALES_DIARY_TRADER$traderId/customers/$customerEmail"
        Mockito.`when`(traderService.findTraderCustomer(traderId, customerEmail)).thenReturn(customerMono)
        shouldGetResponse(testClient, endpoint, authorizationToken)
                .expectStatus().isOk.expectBody()
                .jsonPath("$.name").isEqualTo(testCustomer.name)
                .jsonPath("$.email").isEqualTo(testCustomer.email)
                .jsonPath("$.traderId").isEqualTo(testCustomer.traderId)
                .jsonPath("$.company").isEqualTo(testCustomer.company)
                .jsonPath("$.signaturePath").isEqualTo(testCustomer.signaturePath)
                .jsonPath("$.address").isEqualTo(testCustomer.address)
                .jsonPath("$.location").isEqualTo(testCustomer.location)
                .jsonPath("$.traderId").value<String> {
                    assertTrue("Expect ${testCustomer.traderId} to equals $it",
                            it == testUser.id)
                }
    }

    @Test
    @Order(8)
    fun shouldFindATradersCustomers() {
        val traderId: String = testTrader.id
        val endpoint = "$API$SALES_DIARY_TRADER$traderId/customers"
        val customerFlux: Flux<Customer> = traderService.findTraderCustomers(traderId)
        Mockito.`when`(traderService.findTraderCustomers(traderId)).thenReturn(customerFlux)

        val fluxContent: Flux<CustomerModel> = shouldGetResponse(testClient, endpoint, authorizationToken)
                .expectStatus().isOk.returnResult(CustomerModel::class.java).responseBody

        val href = "$BASE_URL$endpoint?size=1&page=0&sort=id&dir=asc"
        val links: MutableList<Link> = mutableListOf(
                Link.of(href, "first"),
                Link.of(href, "last"),
                Link.of(href, "self"))
        val expectedModel: CustomerModel = CustomerModel(testCustomer).add(links)
        StepVerifier.create(fluxContent)
                .expectNext(expectedModel)
                .verifyComplete()

    }

    @Test
    @Order(9)
    fun shouldUpdateTrader() {
        val requestBody: MutableMap<String, Any> = mutableMapOf("phone" to "09047348344",
                "address" to "updated address 003",
                "location" to mutableListOf(12.00, 14.934))
        val endpoint = "$API$SALES_DIARY_TRADERS"
        val updatedTrader: Trader = testTrader.copy()
        updatedTrader.phone = requestBody["phone"] as String
        updatedTrader.location = mutableListOf()
        (requestBody["location"] as MutableList<*>).forEach {
            updatedTrader.location.add(it as Double)
        }
        val updatedTraderMono: Mono<Trader> = Mono.just(updatedTrader)
        Mockito.`when`(traderService.updateTrader(requestBody)).thenReturn(updatedTraderMono)
        testClient.patch().uri(endpoint)
                .header(AUTHORIZATION, "$PREFIX$authorizationToken")
                .accept(APPLICATION_JSON)
                .body(updatedTraderMono, Trader::class.java)
                .exchange().expectStatus().isOk
                .expectBody()
                .jsonPath("$.phone").isEqualTo(updatedTrader.phone)
                .jsonPath("$.address").isEqualTo(updatedTrader.address)
                .jsonPath("$.location").isEqualTo(updatedTrader.location)

    }

    @Test
    @Order(10)
    fun shouldUpdateTraderProduct() {
        val traderId: String = testTrader.id
        val productId: String = testProduct.id
        val endpoint = "$API$SALES_DIARY_TRADER$traderId/products/$productId"
        val requestBody: MutableMap<String, Any> = mutableMapOf(
                "cost" to 12.00, "stock" to 150.00)
        val updatedProduct: Product = testProduct.copy()
        updatedProduct.cost = requestBody["cost"] as Double
        updatedProduct.stock = requestBody["stock"] as Double
        val updatedProductMono: Mono<Product> = Mono.just(updatedProduct)
        Mockito.`when`(traderService.updateTraderProduct(traderId, requestBody, productId))
                .thenReturn(updatedProductMono)

        testClient.patch().uri(endpoint)
                .header(AUTHORIZATION, "$PREFIX$authorizationToken")
                .accept(APPLICATION_JSON)
                .body(updatedProductMono, Product::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.cost").isEqualTo(updatedProduct.cost)
                .jsonPath("$.stock").isEqualTo(updatedProduct.stock)

    }

    @Test
    @Order(11)
    fun shouldUpdateTraderCustomer() {

        val traderId: String = testTrader.id
        val customerEmail: String = testCustomer.email
        val requestBody: MutableMap<String, Any> = mutableMapOf("address" to "address 004", "company" to "company004")
        val endpoint = "$API$SALES_DIARY_TRADER$traderId/customers/$customerEmail"
        val updatedCustomer: Customer = testCustomer.copy()
        updatedCustomer.address = requestBody["address"] as String
        updatedCustomer.company = requestBody["company"] as String
        val updatedCustomerMono: Mono<Customer> = Mono.just(updatedCustomer)

        Mockito.`when`(traderService.updateTraderCustomer(traderId, requestBody, customerEmail)).thenReturn(updatedCustomerMono)

        testClient.patch().uri(endpoint)
                .header(AUTHORIZATION, "$PREFIX$authorizationToken")
                .accept(APPLICATION_JSON)
                .body(updatedCustomerMono, Customer::class.java)
                .exchange().expectStatus().isOk
                .expectBody()
                .jsonPath("$.address").isEqualTo(updatedCustomer.address)
                .jsonPath("$.company").isEqualTo(updatedCustomer.company)
    }

    @Test
    @Order(12)
    fun shouldDeleteTraderProduct() {
        val traderId: String = testTrader.id
        val productId: String = testProduct.id
        val endpoint = "$API$SALES_DIARY_TRADER$traderId/products/$productId"
        Mockito.`when`(traderService.deleteTraderProduct(traderId, productId)).thenReturn(Mono.empty())

        testClient.delete().uri(endpoint)
                .header(AUTHORIZATION, "$PREFIX$authorizationToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent
    }

    @Test
    @Order(13)
    fun shouldDeleteTraderCustomer() {
        val traderId: String = testTrader.id
        val customerEmail: String = testCustomer.email
        val endpoint = "$API$SALES_DIARY_TRADER$traderId/customers/$customerEmail"
        Mockito.`when`(traderService.deleteTraderCustomer(traderId, customerEmail)).thenReturn(Mono.empty())

        testClient.delete().uri(endpoint)
                .header(AUTHORIZATION, "$PREFIX$authorizationToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent
    }

}

