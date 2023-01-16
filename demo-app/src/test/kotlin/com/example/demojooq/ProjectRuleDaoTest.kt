package com.example.demojooq

import com.example.demojooq.common.BaseDatabaseTest
import com.example.demojooq.data.RuleDto
import com.example.demojooq.data.enums.StatusEnum
import com.example.demojooq.db.ProjectRuleDao
import com.example.demojooq.jooq.generated.tables.pojos.ProjectRule
import com.example.demojooq.jooq.generated.tables.references.PROJECT_RULE
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.test.runTest
import org.jooq.DSLContext
import org.jooq.JSONB
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class ProjectRuleDaoTest : BaseDatabaseTest() {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dslContext: DSLContext

    @Autowired
    private lateinit var dao: ProjectRuleDao

    @AfterEach
    fun clearTable() {
        dslContext.truncate(PROJECT_RULE).execute()
    }

    @Test
    fun testUpsertNew() = runTest {
        println(
            dao.upsert(
                newProjectRule(
                    msg = "test1",
                    status = StatusEnum.THREE
                )
            )
        )
    }

    @Test
    fun testUpsertNewWithId() = runTest {
        println(
            dao.upsert(
                newProjectRule(
                    id = UUID.randomUUID(),
                    msg = "test1",
                    status = StatusEnum.THREE
                )
            )
        )
    }

    @Test
    fun testUpsertExistingWithCorrectVersion() = runTest {
        val ruleId = UUID.randomUUID()
        println(
            dao.upsert(
                newProjectRule(
                    id = ruleId,
                    msg = "test1",
                    status = StatusEnum.THREE
                )
            )
        )
        println(
            dao.upsert(
                newProjectRule(
                    id = ruleId,
                    msg = "test2",
                    status = StatusEnum.ONE,
                    version = 1
                )
            )
        )
    }

    @Test
    fun testUpsertExistingWithIncorrectVersion() = runTest {
        val ruleId = UUID.randomUUID()
        println(
            dao.upsert(
                newProjectRule(
                    id = ruleId,
                    msg = "test1",
                    status = StatusEnum.THREE
                )
            )
        )
        assertThrows<RuntimeException> {
            dao.upsert(
                newProjectRule(
                    id = ruleId,
                    msg = "test2",
                    status = StatusEnum.ONE,
                )
            )
        }
    }

    private fun newProjectRule(
        id: UUID? = null,
        msg: String,
        status: StatusEnum,
        version: Int? = null,
    ): ProjectRule {
        return ProjectRule(
            id = id,
            msg = msg,
            rules = RuleDto(true, setOf("foo", "bar")),
            config = JSONB.jsonb(
                objectMapper.writeValueAsString(
                    mapOf("a" to mapOf("b" to "c"))
                )
            ),
            status = status,
            version = version,
        )
    }
}
