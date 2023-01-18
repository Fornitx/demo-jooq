package com.example.demojooq.db.dao

import com.example.demojooq.db.pojo.ContextPojo
import kotlinx.coroutines.future.await
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import java.util.concurrent.ExecutorService

class ContextHistoryDao(private val dslContext: DSLContext, private val executor: ExecutorService) {
    private val idField = DSL.field("id", SQLDataType.UUID)
    private val versionField = DSL.field("version", SQLDataType.INTEGER)
    private val createdAtField = DSL.field("created_at", SQLDataType.TIMESTAMPWITHTIMEZONE)
    private val updatedAtField = DSL.field("updated_at", SQLDataType.TIMESTAMPWITHTIMEZONE)
    private val msgField = DSL.field("msg", SQLDataType.VARCHAR)
    private val statusField = DSL.field("status", SQLDataType.VARCHAR)
    private val rulesField = DSL.field("rules", SQLDataType.JSONB)
    private val configField = DSL.field("config", SQLDataType.JSONB)

    suspend fun insert(prefix: String, pojo: ContextPojo): Int {
        return dslContext.insertInto(
            DSL.table("${prefix}_context_history"),
            idField,
            versionField,
            createdAtField,
            updatedAtField,
            msgField,
            statusField,
            rulesField,
            configField,
        ).values(
            pojo.id,
            pojo.version,
            pojo.createdAt,
            pojo.updatedAt,
            pojo.msg,
            pojo.status?.name,
            pojo.rules,
            pojo.config,
        )
            .executeAsync(executor)
            .await()
    }
}
