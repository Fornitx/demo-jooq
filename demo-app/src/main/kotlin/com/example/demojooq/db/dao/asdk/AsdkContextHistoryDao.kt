package com.example.demojooq.db.dao.asdk

import com.example.demojooq.jooq.generated.tables.pojos.AsdkContextHistory
import com.example.demojooq.jooq.generated.tables.references.ASDK_CONTEXT_HISTORY
import kotlinx.coroutines.future.await
import org.jooq.DSLContext
import java.util.concurrent.ExecutorService

class AsdkContextHistoryDao(private val dslContext: DSLContext, private val executor: ExecutorService) {
    suspend fun insert(pojo: AsdkContextHistory): Int {
        return dslContext.insertInto(
            ASDK_CONTEXT_HISTORY,
            ASDK_CONTEXT_HISTORY.ID,
            ASDK_CONTEXT_HISTORY.VERSION,
            ASDK_CONTEXT_HISTORY.CREATED_AT,
            ASDK_CONTEXT_HISTORY.UPDATED_AT,
            ASDK_CONTEXT_HISTORY.MSG,
            ASDK_CONTEXT_HISTORY.STATUS,
            ASDK_CONTEXT_HISTORY.RULES,
            ASDK_CONTEXT_HISTORY.CONFIG,
        ).values(
            pojo.id,
            pojo.version,
            pojo.createdAt,
            pojo.updatedAt,
            pojo.msg,
            pojo.status,
            pojo.rules,
            pojo.config,
        )
            .executeAsync(executor)
            .await()
    }
}
