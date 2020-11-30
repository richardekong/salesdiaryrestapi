package com.daveace.salesdiaryrestapi.page

import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.page.ReactivePageSupport.Companion.FIRST
import com.daveace.salesdiaryrestapi.page.ReactivePageSupport.Companion.LAST
import com.daveace.salesdiaryrestapi.page.ReactivePageSupport.Companion.NEXT
import com.daveace.salesdiaryrestapi.page.ReactivePageSupport.Companion.PREV
import com.daveace.salesdiaryrestapi.page.ReactivePageSupport.Companion.SELF
import com.daveace.salesdiaryrestapi.sort.Sort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import javax.validation.constraints.NotNull

@Component
class Paginator : ReactivePageSupport {

    private lateinit var sorter: Sort

    @Autowired
    fun setSorter(sorter: Sort) {
        this.sorter = sorter
    }

    override fun <S : Any, T : RepresentationModel<T>> paginate(
            supportAssembler: RepresentationModelAssemblerSupport<S, T>,
            resourceFlux: Flux<S>,
            pageRequest: PageRequest,
            link: Link,
            sortProps: SortConfigurationProperties): Flux<PagedModel<T>> {

        val sortedResources: MutableList<S> = sortResources(resourceFlux, sortProps)
        val totalElements: Int = sortedResources.size
        val page: Int = pageRequest.pageNumber
        val size: Int = pageRequest.pageSize
        val totalPages: Int = calculateTotalPages(size, totalElements)
        if (page > (totalPages - 1)) throw RuntimeException("No page with: $page")
        val requestedResources: MutableList<S> = Presentable.presentPage(sortedResources, size, page)
        val metadata: PagedModel.PageMetadata = createPagedMetadata(pageRequest, totalElements, totalPages)
        val requestedContents: Collection<T> = supportAssembler.toCollectionModel(requestedResources).content
        val pagedModel: PagedModel<T> = PagedModel.of(requestedContents, metadata)
        addPageLinks(pageRequest, pagedModel, link)
        return Flux.just(pagedModel).subscribeOn(Schedulers.parallel())
    }

    private fun <T : RepresentationModel<T>> addPageLinks(req: PageRequest, pm: PagedModel<T>, lnk: Link) {
        addFirstPageLink(pm, lnk)
        addNextPageLink(req, pm, lnk)
        addPreviousPageLink(req, pm, lnk)
        addLastPageLink(pm, lnk)
        addSelfLink(pm, lnk)
    }

    private fun <T : RepresentationModel<T>> addFirstPageLink(pm: PagedModel<T>, lnk: Link) {
        addPageLink(pm, lnk, 0, FIRST)
    }

    private fun <T : RepresentationModel<T>> addNextPageLink(req: PageRequest, pm: PagedModel<T>, lnk: Link) {
        val last = pm.metadata?.totalPages?.dec()?.toInt()
        if (req.pageNumber < last!!) {
            addPageLink(pm, lnk, req.next().pageNumber, NEXT)
        }
    }

    private fun <T : RepresentationModel<T>> addPreviousPageLink(req: PageRequest, pm: PagedModel<T>, lnk: Link) {
        if (req.hasPrevious()) {
            val previousPage = req.previous().pageNumber
            addPageLink(pm, lnk, previousPage, PREV)
        }
    }

    private fun <T : RepresentationModel<T>> addLastPageLink(pm: PagedModel<T>, lnk: Link) {
        val lastPage = pm.metadata?.totalPages?.dec()?.toInt()
        lastPage?.let {
            addPageLink(pm, lnk, it, LAST)
        }
    }

    private fun <T : RepresentationModel<T>> addSelfLink(pm: PagedModel<T>, lnk: Link) {
        val rel = lnk.rel.toString()
        if (rel == SELF) pm.add(lnk)
        else pm.add(lnk.withSelfRel())
    }

    private fun createPagedMetadata(req: PageRequest, totalElement: Int, totalPages: Int): PagedModel.PageMetadata =
            PagedModel.PageMetadata(req.pageSize.toLong(), req.pageNumber.toLong(), totalElement.toLong(), totalPages.toLong())

    private fun <S : Any> sortResources(resourceFlux: Flux<S>, @NotNull sortProps: SortConfigurationProperties)
            : MutableList<S> {
        return resourceFlux.collectList()
                .map { resources -> sorter.arrangeBy(resources, sortProps.by, sortProps.dir) }
                .toFuture()
                .join()
    }
}

