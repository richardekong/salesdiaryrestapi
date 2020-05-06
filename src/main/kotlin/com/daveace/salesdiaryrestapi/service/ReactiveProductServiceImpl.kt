package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.repository.ReactiveProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ReactiveProductServiceImpl : ReactiveProductService {

    @Autowired
    private lateinit var productRepo: ReactiveProductRepository
}