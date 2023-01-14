package com.example.demojooq.common

import mu.KLogging
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource
import kotlin.reflect.jvm.jvmName

@TestPropertySource(
    properties = [
        "spring.liquibase.change-log=classpath:/db/changelog/changelog-master.xml",
        "spring.liquibase.default-schema=project_schema",
    ]
)
abstract class BaseDatabaseTest {
    companion object : KLogging() {
        protected val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer(
            DockerImageName.parse("postgres:11-alpine")
                .asCompatibleSubstituteFor("postgres")
        )
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("init.sql")
//            .withCreateContainerCmdModifier {
//                it.hostConfig!!.withPortBindings(
//                    PortBinding(
//                        Ports.Binding.bindPort(PostgreSQLContainer.POSTGRESQL_PORT),
//                        ExposedPort(PostgreSQLContainer.POSTGRESQL_PORT)
//                    )
//                )
//            }
            .withReuse(true)

        init {
            postgresContainer.start()

            System.setProperty("POSTGRES_URL", postgresContainer.jdbcUrl + "&TC_REUSABLE=true")
            System.setProperty("POSTGRES_USERNAME", postgresContainer.username)
            System.setProperty("POSTGRES_PASSWORD", postgresContainer.password)

            logger.info("\nPostgres container started: {}", postgresContainer.jdbcUrl)
        }
    }

    protected val log = KotlinLogging.logger(this::class.jvmName)

    @Autowired
    protected lateinit var dataSource: DataSource

    @Autowired
    protected lateinit var template: JdbcTemplate
}
