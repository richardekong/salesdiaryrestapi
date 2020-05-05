package com.daveace.salesdiaryrestapi.config

import com.daveace.salesdiaryrestapi.hateoas.sorting.SortOrder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "sales-diary.sorting")
data class SortConfigurationProperties(
        var by: String = "email",
        var dir: String = SortOrder.ASC.order
)