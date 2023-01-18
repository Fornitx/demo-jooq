package com.example.demojooq.db.dao.asdk

import com.example.demojooq.TestUtils.randomString
import com.example.demojooq.common.AbstractDatabaseTest
import com.example.demojooq.data.enums.StatusEnum
import com.example.demojooq.jooq.generated.tables.pojos.AsdkContextHistory
import com.example.demojooq.jooq.generated.tables.references.ASDK_CONTEXT_HISTORY
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jooq.JSONB
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@SpringBootTest
class AsdkContextHistoryDaoTest : AbstractDatabaseTest() {
    @Autowired
    private lateinit var dao: AsdkContextHistoryDao

    @AfterEach
    fun clearTable() {
        dslContext.truncate(ASDK_CONTEXT_HISTORY).execute()
    }

    @Test
    fun insert() = runTest {
        val pojoToSave = AsdkContextHistory(
            id = UUID.randomUUID(),
            version = 5,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            msg = randomString(),
            status = StatusEnum.THREE,
            rules = null,
            config = JSONB.jsonb("{}"),
        )
        assertThat(dao.insert(pojoToSave)).isOne

        val list = dslContext.selectFrom(ASDK_CONTEXT_HISTORY).fetchInto(AsdkContextHistory::class.java)
        assertThat(list).hasSize(1)
        assertThat(list.first())
            .usingRecursiveComparison()
            .withEqualsForType(
                { a, b -> a.truncatedTo(ChronoUnit.MILLIS).isEqual(b.truncatedTo(ChronoUnit.MILLIS)) },
                OffsetDateTime::class.java
            )
            .isEqualTo(pojoToSave)
    }
}
