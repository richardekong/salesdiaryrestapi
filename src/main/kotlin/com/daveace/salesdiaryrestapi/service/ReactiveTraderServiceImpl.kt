package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ReactiveTraderServiceImpl : ReactiveTraderService{

    @Autowired
    private lateinit var traderRepo:ReactiveTraderRepository

}

