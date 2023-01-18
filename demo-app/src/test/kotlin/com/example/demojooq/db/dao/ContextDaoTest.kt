package com.example.demojooq.db.dao

import com.example.demojooq.common.AbstractDatabaseTest
import com.example.demojooq.data.RuleDto
import com.example.demojooq.data.enums.StatusEnum
import com.example.demojooq.db.pojo.ContextPojo
import com.example.demojooq.jooq.generated.tables.references.ASDK_CONTEXT
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.test.runTest
import org.jooq.JSONB
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import java.util.*

@SpringBootTest
class ContextDaoTest : AbstractDatabaseTest() {
    private val prefix = "asdk"

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dao: ContextDao

    @AfterEach
    fun clearTable() {
        dslContext.truncate(ASDK_CONTEXT).execute()
    }

    @Test
    fun testUpsertNew() = runTest {
        println(
            dao.upsert(
                prefix,
                newContext(
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
                prefix,
                newContext(
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
                prefix,
                newContext(
                    id = ruleId,
                    msg = "test1",
                    status = StatusEnum.THREE
                )
            )
        )
        println(
            dao.upsert(
                prefix,
                newContext(
                    id = ruleId,
                    version = 1,
                    msg = "test2",
                    status = StatusEnum.ONE,
                )
            )
        )
    }

    @Test
    fun testUpsertExistingWithIncorrectVersion() = runTest {
        val ruleId = UUID.randomUUID()
        println(
            dao.upsert(
                prefix,
                newContext(
                    id = ruleId,
                    msg = "test1",
                    status = StatusEnum.THREE
                )
            )
        )
        assertThrows<OptimisticLockingFailureException> {
            dao.upsert(
                prefix,
                newContext(
                    id = ruleId,
                    msg = "test2",
                    status = StatusEnum.ONE,
                )
            )
        }
    }

    private fun newContext(
        id: UUID? = null,
        version: Int? = null,
        msg: String,
        status: StatusEnum? = null,
    ): ContextPojo {
        return ContextPojo(
            id = id,
            version = version,
            msg = msg,
            status = status,
            rules = JSONB.jsonb(
                objectMapper.writeValueAsString(
                    RuleDto(true, setOf("foo", "bar"))
                )
            ),
            config = JSONB.jsonb(
                objectMapper.writeValueAsString(
                    mapOf("a" to mapOf("b" to "c"))
                )
            ),
        )
    }
}
