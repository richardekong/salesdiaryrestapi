package com.daveace.salesdiaryrestapi

import com.daveace.salesdiaryrestapi.domain.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.properties"])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseTests {

    protected fun createTestUser(): User {
        return User(makeFakeEmail(), "testPassword123")
    }

    protected fun createTestTrader(user: User): Trader {
        return Trader(user.id, user.email, "testTrader", "09034734632", "test address")
    }

    protected fun createTestProduct(trader: Trader): Product {
        return Product(trader.id, "Product001", "code0019238", "img/path/product001", 10.00, 100.00)
    }

    protected fun createTestCustomer(trader: Trader): Customer {
        val location: MutableList<Double> = mutableListOf(12.00, 13.00)
        return Customer(makeFakeEmail(), "Customer001", "00233473434", trader.id, "company001", "address001", location)
    }

    protected fun createTestEvent(trader: Trader, product: Product, customer: Customer): SalesEvent {
        return SalesEvent(trader.id, customer.id, product.id, product.name, 2.00, 150.00, 180.00, 5.00, mutableListOf(12.00, 13.00))
    }

    private fun makeFakeEmail(): String {
        return UUID.randomUUID().toString().substring(0, 3).plus("@mail.com")
    }
}