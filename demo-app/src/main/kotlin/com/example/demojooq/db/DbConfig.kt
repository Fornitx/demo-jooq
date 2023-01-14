package com.example.demojooq.db

import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import javax.sql.DataSource

@Configuration
class DbConfig(dataSource: DataSource?) : DisposableBean {
    private val executor: ExecutorService

    init {
        val poolSize = if (dataSource == null || dataSource !is HikariDataSource || dataSource.maximumPoolSize == -1) {
            10
        } else {
            dataSource.maximumPoolSize
        }
        executor = Executors.newFixedThreadPool(poolSize)
    }

    @Bean
    fun projectRuleDao(dslContext: DSLContext): ProjectRuleDao = ProjectRuleDao(dslContext, executor)

    override fun destroy() {
        executor.shutdown()
        if (!executor.awaitTermination(10, SECONDS)) {
            executor.shutdownNow()
        }
    }
}
