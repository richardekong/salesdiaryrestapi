package com.daveace.salesdiaryrestapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class SalesDiaryRestApiApplication

fun main(args: Array<String>) {
	runApplication<SalesDiaryRestApiApplication>(*args)
}
