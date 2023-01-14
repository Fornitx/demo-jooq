package com.example.demojooq

import com.example.demojooq.common.BaseDatabaseTest
import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DataSourceTest : BaseDatabaseTest() {
    @Test
    fun test() {
        log.info("\ndataSource = {}", dataSource)

        assertThat(dataSource).isInstanceOf(HikariDataSource::class.java)

        val hikariDataSource = dataSource as HikariDataSource
        assertEquals(10, hikariDataSource.maximumPoolSize)
    }
}
