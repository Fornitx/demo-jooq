package com.example.demojooq.db.dao.asdk

import com.example.demojooq.jooq.generated.tables.pojos.AsdkContext
import com.example.demojooq.jooq.generated.tables.references.ASDK_CONTEXT
import kotlinx.coroutines.future.await
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.dao.OptimisticLockingFailureException
import java.util.*
import java.util.concurrent.ExecutorService

class AsdkContextDao(private val dslContext: DSLContext, private val executor: ExecutorService) {
    suspend fun upsert(pojo: AsdkContext): AsdkContext {
        if (pojo.id == null) {
            return dslContext.insertInto(
                ASDK_CONTEXT,
                ASDK_CONTEXT.MSG,
                ASDK_CONTEXT.STATUS,
                ASDK_CONTEXT.RULES,
                ASDK_CONTEXT.CONFIG,
            ).values(
                pojo.msg,
                pojo.status,
                pojo.rules,
                pojo.config,
            )
                .returning()
                .fetchAsync(executor)
                .await()
                .into(AsdkContext::class.java)
                .single()
        }
        return dslContext.insertInto(
            ASDK_CONTEXT,
            ASDK_CONTEXT.ID,
            ASDK_CONTEXT.MSG,
            ASDK_CONTEXT.STATUS,
            ASDK_CONTEXT.RULES,
            ASDK_CONTEXT.CONFIG,
        ).values(
            pojo.id,
            pojo.msg,
            pojo.status,
            pojo.rules,
            pojo.config,
        )
            .onConflict(ASDK_CONTEXT.ID)
            .doUpdate()
            .set(ASDK_CONTEXT.VERSION, ASDK_CONTEXT.VERSION.plus(1))
            .set(ASDK_CONTEXT.MSG, DSL.excluded(ASDK_CONTEXT.MSG))
            .set(ASDK_CONTEXT.STATUS, DSL.excluded(ASDK_CONTEXT.STATUS))
            .set(ASDK_CONTEXT.RULES, DSL.excluded(ASDK_CONTEXT.RULES))
            .set(ASDK_CONTEXT.CONFIG, DSL.excluded(ASDK_CONTEXT.CONFIG))
            .set(ASDK_CONTEXT.UPDATED_AT, DSL.currentOffsetDateTime())
            .where(ASDK_CONTEXT.VERSION.equal(pojo.version))
            .returning()
            .fetchAsync(executor)
            .await()
            .into(AsdkContext::class.java)
            .singleOrNull()
            ?: throw OptimisticLockingFailureException("Context version failed [id=${pojo.id}, version=${pojo.version}]")
    }

    suspend fun delete(ids: Set<UUID>): Set<UUID> {
        return dslContext.deleteFrom(ASDK_CONTEXT)
            .where(ASDK_CONTEXT.ID.`in`(ids))
            .returningResult(ASDK_CONTEXT.ID)
            .fetchAsync(executor)
            .await()
            .intoSet(ASDK_CONTEXT.ID) as Set<UUID>
    }
}
