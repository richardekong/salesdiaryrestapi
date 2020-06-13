package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.SalesMetrics
import org.springframework.hateoas.RepresentationModel

data class SalesMetricsModel(val metrics: SalesMetrics): RepresentationModel<SalesMetricsModel>()