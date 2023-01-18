package com.example.demojooq

import com.example.demojooq.common.AbstractDatabaseTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JdbcTemplateTest : AbstractDatabaseTest() {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "select current_schema()",
            "show search_path",
            "select version()",
            "select uuid_generate_v4()",
            "select * from databasechangelog",
        ]
    )
    fun info(sql: String) {
        val result = jdbcTemplate.queryForList(sql)
        log.info("\nresult =\n{}", result.joinToString(System.lineSeparator()))
    }
}
