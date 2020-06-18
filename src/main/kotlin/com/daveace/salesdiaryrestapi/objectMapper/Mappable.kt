package com.daveace.salesdiaryrestapi.objectMapper

import com.fasterxml.jackson.databind.ObjectMapper

interface Mappable {

    fun toMap(): MutableMap<String, Any?> {
        return ObjectMapper().convertValue(this, getMapInstance()::class.java)
    }

    private fun getMapInstance(): MutableMap<String, Any?> {
        return mutableMapOf()
    }

}

