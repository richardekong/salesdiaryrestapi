//package com.daveace.salesdiaryrestapi.controllertest
//
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_PRODUCT
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_PRODUCTS
//import com.daveace.salesdiaryrestapi.controller.ProductController
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.populateReactiveRepository
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldDeleteEntity
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPatchEntity
//import com.daveace.salesdiaryrestapi.domain.Product
//import com.daveace.salesdiaryrestapi.repository.ReactiveProductRepository
//import com.daveace.salesdiaryrestapi.service.ReactiveProductServiceImpl
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.junit.jupiter.api.extension.ExtendWith
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.context.annotation.Import
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import org.springframework.test.web.reactive.server.WebTestClient
//import reactor.core.publisher.Flux
//import java.util.*
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPostEntity
//import com.daveace.salesdiaryrestapi.hateoas.model.ProductModel
//import org.springframework.beans.factory.annotation.Value
//
//@ExtendWith(SpringExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class ProductControllerTest {
//
//    @MockBean
//    private lateinit var productRepo: ReactiveProductRepository
//    private lateinit var testClient: WebTestClient
//
//    private fun createTestProduct(): Product {
//        val product = Product("Dell Inspiron")
//        product.code = UUID.randomUUID().toString().substring(11)
//        product.cost = 500.00
//        product.stock = 20.0
//        product.imagePath = "c:/fake_img_path"
//        product.traderId = UUID.randomUUID().toString()
//        return product
//    }
//
//    private fun createTestProducts(): Array<Product> {
//        return arrayOf(
//                Product("Samsung Galaxy"),
//                Product("Dell XPS"),
//                Product("Hp")
//        )
//    }
//
//    @BeforeAll
//    fun init() {
//        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
//        populateReactiveRepository(productRepo, createTestProducts().asList())
//    }
//
//    @Test
//    fun shouldInsertProduct() {
//        val endpoint = "$API$SALES_DIARY_PRODUCTS"
//        val productToInsert = createTestProduct()
//        shouldPostEntity(productToInsert, productRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$.id").isEqualTo(productToInsert.id)
//                .jsonPath("$.name").isEqualTo(productToInsert.name)
//                .jsonPath("$.code").isEqualTo(productToInsert.code)
//                .jsonPath("$.cost").isEqualTo(productToInsert.cost)
//                .jsonPath("$.stock").isEqualTo(productToInsert.stock)
//                .jsonPath("$.imagePath").isEqualTo(productToInsert.imagePath)
//                .jsonPath("$.traderId").isEqualTo(productToInsert.traderId)
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//    }
//
//    @Test
//    fun shouldGetProduct() {
//        val productToGet = createTestProduct()
//        val endpoint = "$API$SALES_DIARY_PRODUCT${productToGet.id}"
//        shouldGetEntity(productToGet.id, productToGet, productRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$.id").isEqualTo(productToGet.id)
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//
//    }
//
//    @Test
//    fun shouldGetProducts() {
//        val productFlux = productRepo.findAll()
//        val endpoint = "$API$SALES_DIARY_PRODUCTS"
//        shouldGetEntities(productFlux, productRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$").isArray
//                .jsonPath("$").isNotEmpty
//                .jsonPath("$..links").exists()
//                .jsonPath("$..links").isArray
//                .jsonPath("$..links[0].rel").exists()
//                .jsonPath("$..links[0].href").exists()
//    }
//
//    @Test
//    fun shouldUpdateProduct() {
//
//        val oldProduct = createTestProduct()
//        val newProduct = oldProduct.copy()
//        val endpoint = "$SALES_DIARY_PRODUCTS?id=${oldProduct.id}"
//        val newStock = 40
//        newProduct.stock += newStock
//        shouldPatchEntity(oldProduct, newProduct, productRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$.stock").isEqualTo(oldProduct.stock + newStock)
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//
//    }
//
//    @Test
//    fun shouldDeleteProduct() {
//        val productToDelete = createTestProducts()[1]
//        val endpoint = "$SALES_DIARY_PRODUCT${productToDelete.id}"
//        shouldDeleteEntity(productToDelete.id, testClient, productRepo, endpoint)
//    }
//}