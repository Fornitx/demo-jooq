package com.example.demojooq.db

import kotlinx.coroutines.future.await
import org.jooq.DSLContext
import org.jooq.Record1
import org.jooq.generated.tables.pojos.ProjectRule
import org.jooq.generated.tables.references.PROJECT_RULE
import org.jooq.impl.DSL
import org.jooq.impl.DSL.excluded
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ExecutorService

class ProjectRuleDao(private val dslContext: DSLContext, private val executor: ExecutorService) {
    suspend fun upsert(projectRule: ProjectRule): ProjectRule {
        if (projectRule.id == null) {
            return dslContext.insertInto(
                PROJECT_RULE,
                PROJECT_RULE.MSG,
                PROJECT_RULE.RULES,
                PROJECT_RULE.CONFIG,
                PROJECT_RULE.STATUS,
            ).values(
                projectRule.msg,
                projectRule.rules,
                projectRule.config,
                projectRule.status,
            )
                .returning()
                .fetchAsync(executor)
                .await()
                .into(ProjectRule::class.java)
                .single()
        }
        return dslContext.insertInto(
            PROJECT_RULE,
            PROJECT_RULE.ID,
            PROJECT_RULE.MSG,
            PROJECT_RULE.RULES,
            PROJECT_RULE.CONFIG,
            PROJECT_RULE.STATUS,
        ).values(
            projectRule.id, projectRule.msg, projectRule.rules, projectRule.config, projectRule.status,
        )
            .onConflict(PROJECT_RULE.ID)
            .doUpdate()
            .set(PROJECT_RULE.MSG, excluded(PROJECT_RULE.MSG))
            .set(PROJECT_RULE.RULES, excluded(PROJECT_RULE.RULES))
            .set(PROJECT_RULE.CONFIG, excluded(PROJECT_RULE.CONFIG))
            .set(PROJECT_RULE.STATUS, excluded(PROJECT_RULE.STATUS))
            .set(PROJECT_RULE.VERSION, PROJECT_RULE.VERSION.plus(1))
            .set(PROJECT_RULE.UPDATED_AT, DSL.currentOffsetDateTime())
            .where(PROJECT_RULE.VERSION.equal(projectRule.version))
            .returning()
            .fetchAsync(executor)
            .await()
            .into(ProjectRule::class.java)
            .singleOrNull() ?: throw RuntimeException("Optimistic Lock")
    }

    suspend fun delete(ids: Set<UUID>): Set<UUID> {
        return dslContext.deleteFrom(PROJECT_RULE)
            .where(PROJECT_RULE.ID.`in`(ids))
            .returningResult(PROJECT_RULE.ID)
            .fetchAsync(executor)
            .await()
            .intoSet(Record1<UUID?>::component1)
    }
}
