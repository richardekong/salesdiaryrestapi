package com.daveace.salesdiaryrestapi.hateoas.paging

import java.lang.RuntimeException
import javax.validation.constraints.NotNull

interface Presentable {
    companion object {
        fun <T : Any> presentPage(@NotNull info: MutableList<T>, sizePerPage: Int, page: Int): MutableList<T> {
            val totalItem: Int = info.size
            val from: Int = sizePerPage * page
            val to: Int = (from + sizePerPage).coerceAtMost(totalItem)
            if (sizePerPage > totalItem) throw RuntimeException("Invalid size per page")
            return info.subList(from, to)
        }
    }
}