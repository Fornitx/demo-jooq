package com.example.demojooq.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.JSONB
import org.jooq.impl.AbstractConverter

class JsonbMapConverter : AbstractConverter<JSONB, Map<*, *>>(JSONB::class.java, Map::class.java) {
    private val mapper: ObjectMapper = ObjectMapper().findAndRegisterModules()

    override fun from(databaseObject: JSONB?): Map<*, *>? {
        if (databaseObject == null) {
            return null
        }
        return mapper.readValue<Map<*, *>>(databaseObject.data())
    }

    override fun to(userObject: Map<*, *>?): JSONB? {
        if (userObject == null) {
            return null
        }
        return JSONB.jsonb(mapper.writeValueAsString(userObject))
    }
}
