package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.page.Paginator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BaseController: ReactiveLinkSupport {

    lateinit var paginator: Paginator
    lateinit var sortProps: SortConfigurationProperties
    lateinit var tokenUtil: TokenUtil
    lateinit var authenticatedUser: AuthenticatedUser

    companion object {
        const val DEFAULT_SIZE = "1"
        const val DEFAULT_PAGE = "0"
        const val DEFAULT_SORT_FIELD = "id"
        const val DEFAULT_SORT_ORDER = "asc"
    }

    @Autowired
    fun initPaginator(paginator: Paginator) {
        this.paginator = paginator
    }

    @Autowired
    fun initSortConfigurationProperties(sortProps:SortConfigurationProperties){
        this.sortProps = sortProps
    }

    @Autowired
    fun initTokenUtil(tokenUtil: TokenUtil){
        this.tokenUtil = tokenUtil
    }

    @Autowired
    fun initAuthenticatedUser(authenticatedUser: AuthenticatedUser){
        this.authenticatedUser = authenticatedUser
    }

    protected fun configureSortProperties(by: String, dir: String): SortConfigurationProperties {
        sortProps.by = by
        sortProps.dir = dir
        return sortProps
    }
}