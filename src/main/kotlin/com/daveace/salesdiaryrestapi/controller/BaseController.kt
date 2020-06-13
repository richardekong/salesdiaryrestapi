package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.page.Paginator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BaseController: ReactiveLinkSupport {

    @Autowired
    open lateinit var paginator: Paginator
    @Autowired
    open lateinit var sortProps: SortConfigurationProperties
    @Autowired
    open lateinit var tokenUtil: TokenUtil

    companion object {
        const val DEFAULT_SIZE = "1"
        const val DEFAULT_PAGE = "0"
        const val DEFAULT_SORT_FIELD = "id"
        const val DEFAULT_SORT_ORDER = "asc"
    }

    protected fun configureSortProperties(by: String, dir: String): SortConfigurationProperties {
        sortProps.by = by
        sortProps.dir = dir
        return sortProps
    }
}