package com.daveace.salesdiaryrestapi.sort

import org.springframework.stereotype.Component
import java.lang.reflect.Field

@Component
class Sort : Arrangeable {

    companion object{
        private val DESC = SortOrder.DESC.order
    }

    override fun <T : Any> arrangeBy(items: MutableList<T>, property: String): MutableList<T> {
        items.sortWith(makeComparator(property))
        return items
    }

    override fun <T : Any> arrangeBy(items: MutableList<T>, property: String, order: String): MutableList<T> {
        return if (!order.equals(DESC, true)) arrangeBy(items, property)
        else arrangeBy(items, property).asReversed()
    }

    private fun reflectAndCompare(item1: Any, item2: Any, property: String): Int {
        val f1: Field = item1.javaClass.getDeclaredField(property)
        val f2: Field = item2.javaClass.getDeclaredField(property)
        setAccessible(true, f1, f2)
        val comparedValue: Int = f1.get(item1).toString()
                .compareTo(f2.get(item2).toString())
        setAccessible(false, f1, f2)
        return comparedValue
    }

    private fun <T : Any> makeComparator(prop: String): Comparator<T> {
        return Comparator { it1, it2 -> reflectAndCompare(it1, it2, prop) }
    }

    private fun setAccessible(accessible: Boolean, vararg fields: Field) {
        fields.forEach { it.isAccessible = accessible }
    }
}