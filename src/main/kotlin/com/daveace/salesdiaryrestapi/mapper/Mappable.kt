package com.daveace.salesdiaryrestapi.mapper

import com.fasterxml.jackson.databind.ObjectMapper

interface Mappable {

    fun toMap(): MutableMap<String, Any?> {
        return ObjectMapper().convertValue(this, getMapInstance()::class.java)
    }

    private fun getMapInstance(): MutableMap<String, Any?> = mutableMapOf()


}

