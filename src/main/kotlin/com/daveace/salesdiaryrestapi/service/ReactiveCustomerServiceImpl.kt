package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.repository.ReactiveCustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ReactiveCustomerServiceImpl : ReactiveCustomerService {

    @Autowired
    private lateinit var customerRepo:ReactiveCustomerRepository
}