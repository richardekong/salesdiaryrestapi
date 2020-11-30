package com.daveace.salesdiaryrestapi.page

import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*


interface ReactivePageSupport {

    companion object {
        const val PAGE = "page"
        const val FIRST = "first"
        const val NEXT = "next"
        const val PREV = "prev"
        const val LAST = "last"
        const val SELF = "self"
        const val ASC = "asc"
        const val DESC = "desc"
        const val QUERY_SYMBOL = "?"
        const val QUERY_DELIMITER = "&"
        const val QUERY_VALUE_PAIR_DEL = "="
    }

    fun <S : Any, T : RepresentationModel<T>> paginate(
            supportAssembler: RepresentationModelAssemblerSupport<S, T>,
            resourceFlux: Flux<S>,
            pageRequest: PageRequest,
            link: Link,
            sortProps: SortConfigurationProperties
    ): Flux<PagedModel<T>>

    fun calculateTotalPages(size: Int, totalElement: Int): Int {
        val remainder = totalElement % size
        val pages = totalElement / size
        val remains = (size > 0 && remainder > 0)
        return if (remains) return (pages + 1) else pages
    }

    fun extractParams(link: Link): Flux<MutableList<MutableMap.MutableEntry<String, String>>> {
        var params: MutableList<MutableMap.MutableEntry<String, String>> = ArrayList()
        val href = link.href
        if (href.isNotEmpty() && href.contains(QUERY_SYMBOL)) {
            val queryStr = href.substring(href.indexOf(QUERY_SYMBOL))
            params = queryStr.split(QUERY_DELIMITER).asSequence()
                    .map { it.split(QUERY_VALUE_PAIR_DEL) }
                    .map { AbstractMap.SimpleEntry<String, String>(it[0], it[1]) }
                    .toMutableList()
        }
        return Flux.just(params)
    }

    fun setPageInQueryParams(
            queryParams: MutableList<MutableMap.MutableEntry<String, String>>,
            page: Int
    ): Flux<MutableList<MutableMap.MutableEntry<String, String>>> {
        queryParams.let { entries: MutableList<MutableMap.MutableEntry<String, String>> ->
            entries.forEach { if (it.key == PAGE) it.setValue(page.toString()) }
        }
        return Flux.just(queryParams)
    }

    fun buildURIQueryString(queryParams: MutableList<MutableMap.MutableEntry<String, String>>): Mono<String> {
        val strBuffer = StringBuffer()
        val lastIndex = queryParams.size - 1
        queryParams.let { entries: MutableList<MutableMap.MutableEntry<String, String>> ->
            entries.forEach {
                var queryStr = "${it.key}=${it.value}"
                if (entries.indexOf(it) < lastIndex)
                    queryStr = queryStr.plus(QUERY_DELIMITER)
                strBuffer.append(queryStr)
            }
        }
        return Mono.just(strBuffer.toString())
    }

    fun reBuildLink(originalHref: String, queryStr: String, rel: String): Mono<Link> = Mono.just(Link.of(originalHref.plus(queryStr), rel))

    fun extractURIWithoutParams(link: Link): Mono<String> = Mono.just(link.href.substring(0, link.href.indexOf(QUERY_SYMBOL)))

    fun <T : RepresentationModel<T>> addPageLink(pm: PagedModel<T>, lnk: Link, page: Int, rel: String) {
        extractURIWithoutParams(lnk).subscribe { originalHref ->
            extractParams(lnk).subscribe { queryParams ->
                setPageInQueryParams(queryParams, page).subscribe { queryParamsWithPage ->
                    buildURIQueryString(queryParamsWithPage).subscribe { queryString ->
                        reBuildLink(originalHref, queryString, rel).subscribe {
                            pm.add(it)
                        }
                    }
                }
            }
        }
    }

}

