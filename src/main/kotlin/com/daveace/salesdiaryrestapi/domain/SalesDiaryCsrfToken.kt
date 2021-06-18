package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.mapper.Mappable
import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.Id

@Document
data class SalesDiaryCsrfToken(
    @Id
    var sessionId:String = "",
    var token:String = ""
): Mappable