package com.daveace.salesdiaryrestapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "sales-diary.paging")
data class PageConfigurationProperties(
        var size: Int = 5,
        var page: Int = 0
)