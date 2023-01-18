package com.example.demojooq.db.pojo

import com.example.demojooq.data.enums.StatusEnum
import org.jooq.JSONB
import java.time.OffsetDateTime
import java.util.*

data class ContextPojo(
    val id: UUID? = null,
    val version: Int? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    val msg: String? = null,
    val status: StatusEnum? = null,
    val rules: JSONB? = null,
    val config: JSONB? = null
)
