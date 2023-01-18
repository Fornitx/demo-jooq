package com.example.demojooq.db.dao.asdk

import com.example.demojooq.common.AbstractDatabaseTest
import com.example.demojooq.data.RuleDto
import com.example.demojooq.data.enums.StatusEnum
import com.example.demojooq.jooq.generated.tables.pojos.AsdkContext
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
class AsdkContextDaoTest : AbstractDatabaseTest() {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dao: AsdkContextDao

    @AfterEach
    fun clearTable() {
        dslContext.truncate(ASDK_CONTEXT).execute()
    }

    @Test
    fun testUpsertNew() = runTest {
        println(
            dao.upsert(
                newAsdkContext(
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
                newAsdkContext(
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
                newAsdkContext(
                    id = ruleId,
                    msg = "test1",
                    status = StatusEnum.THREE
                )
            )
        )
        println(
            dao.upsert(
                newAsdkContext(
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
                newAsdkContext(
                    id = ruleId,
                    msg = "test1",
                    status = StatusEnum.THREE
                )
            )
        )
        assertThrows<OptimisticLockingFailureException> {
            dao.upsert(
                newAsdkContext(
                    id = ruleId,
                    msg = "test2",
                    status = StatusEnum.ONE,
                )
            )
        }
    }

    private fun newAsdkContext(
        id: UUID? = null,
        version: Int? = null,
        msg: String,
        status: StatusEnum? = null,
    ): AsdkContext {
        return AsdkContext(
            id = id,
            version = version,
            msg = msg,
            status = status,
            rules = RuleDto(true, setOf("foo", "bar")),
            config = JSONB.jsonb(
                objectMapper.writeValueAsString(
                    mapOf("a" to mapOf("b" to "c"))
                )
            ),
        )
    }
}
