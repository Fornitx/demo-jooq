package com.example.demojooq.gradle

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import mu.KotlinLogging
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.io.PrintWriter
import java.sql.DriverManager

private val log = KotlinLogging.logger {}

class ManualJooqTask {
    @Test
    fun run() {
        val container = startContainer()
        runLiquibase(container)
        runJooq(container)
        println(1)
    }

    private fun startContainer(): PostgreSQLContainer<*> {
        val container = PostgreSQLContainer(
            DockerImageName.parse("postgres:11-alpine").asCompatibleSubstituteFor("postgres")
        )
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("init.sql")
            .withCreateContainerCmdModifier {
                it.hostConfig!!.withPortBindings(
                    PortBinding(
                        Ports.Binding.bindPort(PostgreSQLContainer.POSTGRESQL_PORT),
                        ExposedPort(PostgreSQLContainer.POSTGRESQL_PORT)
                    )
                )
            }

        container.start()
        log.info {
            "\nContainer started:\n" +
                "\tURL: ${container.jdbcUrl}\n" +
                "\tUser:${container.username}\n" +
                "\tPassword: ${container.password}"
        }
        return container
    }

    private fun runLiquibase(container: PostgreSQLContainer<*>) {
        val connection = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)

        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(connection))

        database.defaultSchemaName = "project_schema"
        Liquibase("db/changelog/changelog-master.xml", ClassLoaderResourceAccessor(), database).update()
    }

    private fun runJooq(container: PostgreSQLContainer<*>) {
        val configuration = Configuration()
            .withJdbc(
                Jdbc()
                    .withDriver("org.postgresql.Driver")
                    .withUrl(container.jdbcUrl)
                    .withUsername(container.username)
                    .withPassword(container.password)
            )
            .withGenerator(
                Generator().withDatabase(
                    Database()
                        .withName("org.jooq.meta.postgres.PostgresDatabase")
                        .withIncludes(".*")
                        .withExcludes("")
                        .withInputSchema("project_schema")
                )
            )
        GenerationTool.generate(configuration)
    }
}
