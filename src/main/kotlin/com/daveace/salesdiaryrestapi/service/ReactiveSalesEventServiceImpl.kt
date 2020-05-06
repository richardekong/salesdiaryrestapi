package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.repository.ReactiveSalesEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ReactiveSalesEventServiceImpl : ReactiveSalesEventService{

    @Autowired
    private lateinit var salesEventRepo: ReactiveSalesEventRepository
}