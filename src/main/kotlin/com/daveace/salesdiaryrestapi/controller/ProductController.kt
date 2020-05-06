package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.daveace.salesdiaryrestapi.service.ReactiveProductService

@RestController
@RequestMapping(API)
class ProductController {

    @Autowired
    private lateinit var productService:ReactiveProductService
}