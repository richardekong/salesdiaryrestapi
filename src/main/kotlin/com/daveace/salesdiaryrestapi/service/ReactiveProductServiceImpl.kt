package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.repository.ReactiveProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import org.springframework.http.HttpStatus
import javax.validation.constraints.NotNull

@Service
class ReactiveProductServiceImpl : ReactiveProductService {

    @Autowired
    private lateinit var productRepo: ReactiveProductRepository

    @Autowired
    private lateinit var authenticatedUser: AuthenticatedUser


    override fun save(@NotNull product: Product): Mono<Product> {
        return productRepo.save(product)
    }

    override fun saveIfAbsent(product: Product): Mono<Product> {
        return existsByName(product.name)
                .filter { productExists ->
                    if (productExists) throw RuntimeException(
                            "${product.name} exists")
                    productExists.not()
                }.flatMap {
                    productRepo.save(product)
                }.switchIfEmpty(
                        Mono.fromRunnable {
                            throw RuntimeException("Failed to save product!")
                        }
                )
    }

    override fun existsByName(name: String): Mono<Boolean> {
        return productRepo.existsByName(name)
    }

    override fun findProduct(id: String): Mono<Product> {
        return authenticatedUser.getCurrentUser()
                .flatMap { currentUser ->
                    productRepo.findById(id)
                            .filter { product ->
                                currentUser.id == product.traderId
                            }
                            .switchIfEmpty(Mono.fromRunnable {
                                throw RestException(
                                        HttpStatus.UNAUTHORIZED.reasonPhrase)
                            })
                }
    }
}