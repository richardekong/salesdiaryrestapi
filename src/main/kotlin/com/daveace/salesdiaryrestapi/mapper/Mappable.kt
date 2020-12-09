package com.daveace.salesdiaryrestapi.mapper

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

interface Mappable {

    fun toMap(): MutableMap<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        this::class.memberProperties.parallelStream()
                .forEach {
                    it.isAccessible = true
                    map[it.name] = it.getter.call(this)
                    it.isAccessible = false
                }
        return map
    }

}

