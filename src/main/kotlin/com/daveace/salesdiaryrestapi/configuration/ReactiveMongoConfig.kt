package com.daveace.salesdiaryrestapi.configuration

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories(basePackages = ["com.daveace.salesdiaryrestapi.repository"])
class ReactiveMongoConfig : AbstractReactiveMongoConfiguration() {

    @Value("\${spring.data.mongodb.database}")
    lateinit var dbName:String

    override fun reactiveMongoClient(): MongoClient {
        return MongoClients.create()
    }

    override fun getDatabaseName(): String {
        return dbName
    }

    @Bean
    fun salesDiaryReactiveMongoTemplate():ReactiveMongoTemplate{
        return ReactiveMongoTemplate(reactiveMongoClient(), databaseName)
    }

}