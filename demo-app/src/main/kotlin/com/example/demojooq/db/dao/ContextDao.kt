package com.example.demojooq.db.dao

import com.example.demojooq.db.pojo.ContextPojo
import kotlinx.coroutines.future.await
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.springframework.dao.OptimisticLockingFailureException
import java.util.*
import java.util.concurrent.ExecutorService

class ContextDao(private val dslContext: DSLContext, private val executor: ExecutorService) {
    private val idField = DSL.field("id", SQLDataType.UUID)
    private val versionField = DSL.field("version", SQLDataType.INTEGER)
    private val createdAtField = DSL.field("created_at", SQLDataType.TIMESTAMPWITHTIMEZONE)
    private val updatedAtField = DSL.field("updated_at", SQLDataType.TIMESTAMPWITHTIMEZONE)
    private val msgField = DSL.field("msg", SQLDataType.VARCHAR)
    private val statusField = DSL.field("status", SQLDataType.VARCHAR)
    private val rulesField = DSL.field("rules", SQLDataType.JSONB)
    private val configField = DSL.field("config", SQLDataType.JSONB)

    private val allFields = listOf(
        idField, versionField, createdAtField, updatedAtField, msgField, statusField, rulesField, configField
    )

    suspend fun upsert(prefix: String, pojo: ContextPojo): ContextPojo {
        if (pojo.id == null) {
            val result = dslContext.insertInto(
                DSL.table("${prefix}_context"),
                msgField,
                statusField,
                rulesField,
                configField,
            ).values(
                pojo.msg,
                pojo.status?.name,
                pojo.rules,
                pojo.config,
            )
                .returning(allFields)
                .fetchAsync(executor)
                .await()
            return result
                .into(ContextPojo::class.java)
                .single()
        }
        val fixedVersionField = DSL.field("${prefix}_context.version", SQLDataType.INTEGER)
        return dslContext.insertInto(
            DSL.table("${prefix}_context"),
            idField,
            msgField,
            statusField,
            rulesField,
            configField,
        ).values(
            pojo.id,
            pojo.msg,
            pojo.status?.name,
            pojo.rules,
            pojo.config,
        )
            .onConflict(idField)
            .doUpdate()
            .set(versionField, fixedVersionField.plus(1))
            .set(msgField, DSL.excluded(msgField))
            .set(statusField, DSL.excluded(statusField))
            .set(rulesField, DSL.excluded(rulesField))
            .set(configField, DSL.excluded(configField))
            .set(updatedAtField, DSL.currentOffsetDateTime())
            .where(fixedVersionField.equal(pojo.version))
            .returning(allFields)
            .fetchAsync(executor)
            .await()
            .into(ContextPojo::class.java)
            .singleOrNull()
            ?: throw OptimisticLockingFailureException("Context version failed [id=${pojo.id}, version=${pojo.version}]")
    }

    suspend fun delete(prefix: String, ids: Set<UUID>): Set<UUID> {
        return dslContext.deleteFrom(DSL.table("${prefix}_context"))
            .where(idField.`in`(ids))
            .returningResult(idField)
            .fetchAsync(executor)
            .await()
            .intoSet(idField)
    }
}
