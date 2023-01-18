package com.example.demojooq.db

import com.example.demojooq.db.dao.ContextDao
import com.example.demojooq.db.dao.ContextHistoryDao
import com.example.demojooq.db.dao.asdk.AsdkContextDao
import com.example.demojooq.db.dao.asdk.AsdkContextHistoryDao
import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer
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
    fun asdkContextDao(dslContext: DSLContext) = AsdkContextDao(dslContext, executor)

    @Bean
    fun asdkContextHistoryDao(dslContext: DSLContext) = AsdkContextHistoryDao(dslContext, executor)

    @Bean
    fun contextDao(dslContext: DSLContext) = ContextDao(dslContext, executor)

    @Bean
    fun contextHistoryDao(dslContext: DSLContext) = ContextHistoryDao(dslContext, executor)

    @Bean
    fun defaultConfigurationCustomizer() = DefaultConfigurationCustomizer { conf ->
//            conf.settings().withRenderMapping(
//                RenderMapping().withSchemata(
//                    MappedSchema().withInput()
//                        .withOutput()
//                )
//            )
    }

    override fun destroy() {
        executor.shutdown()
        if (!executor.awaitTermination(10, SECONDS)) {
            executor.shutdownNow()
        }
    }
}
