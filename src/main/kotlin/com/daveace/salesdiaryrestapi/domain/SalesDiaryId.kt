package com.daveace.salesdiaryrestapi.domain

import java.util.*

interface SalesDiaryId {

    companion object {

        fun generateId(): String = UUID.randomUUID().toString().replace("\u002D", "")
    }
}