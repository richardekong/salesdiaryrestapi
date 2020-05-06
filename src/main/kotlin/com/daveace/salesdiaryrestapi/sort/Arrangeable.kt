package com.daveace.salesdiaryrestapi.hateoas.sorting

interface Arrangeable {

    fun <T:Any> arrangeBy(items:MutableList<T>, property:String):MutableList<T>

    fun <T:Any> arrangeBy(items:MutableList<T>, property:String, order:String):MutableList<T>
}