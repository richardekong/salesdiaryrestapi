package com.daveace.salesdiaryrestapi.page

import java.lang.RuntimeException
import javax.validation.constraints.NotNull

interface Presentable {
    companion object {
        fun <T : Any> presentPage(@NotNull info: MutableList<T>, sizePerPage: Int, page: Int): MutableList<T> {
            val totalItem: Int = info.size
            val from: Int = sizePerPage * page
            var to: Int = (from + sizePerPage).coerceAtMost(totalItem)
            if (sizePerPage > totalItem) {
                to = (from + totalItem).coerceAtMost(totalItem)
            }
            return info.subList(from, to)
        }
    }
}