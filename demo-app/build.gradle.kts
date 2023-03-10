plugins {
    id("org.springframework.boot") version System.getProperty("springVersion")
    id("io.spring.dependency-management") version System.getProperty("springDMVersion")
    kotlin("jvm") version System.getProperty("kotlinVersion")
    kotlin("plugin.spring") version System.getProperty("kotlinVersion")

    id("nu.studer.jooq") version "8.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

val jooqVersion = dependencyManagement.importedProperties["jooq.version"]
val postgresVersion = dependencyManagement.importedProperties["postgresql.version"]
val testcontainersVersion = "1.17.6"
//extra["testcontainersVersion"] = "1.17.6"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

    implementation("org.jooq:jooq-jackson-extensions")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")

    testImplementation(project(":demo-db"))

    jooqGenerator("org.slf4j:slf4j-simple")
    jooqGenerator("org.postgresql:postgresql:$postgresVersion")
    jooqGenerator("org.testcontainers:postgresql:$testcontainersVersion")
    jooqGenerator(project(":demo-db"))

    testImplementation("org.jooq:jooq-codegen:$jooqVersion")
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "19"
    }
}

tasks.test {
//    environment("TESTCONTAINERS_RYUK_DISABLED", true)
    environment("TESTCONTAINERS_CHECKS_DISABLE", true)
    systemProperty("org.jooq.no-logo", true)
    systemProperty("org.jooq.no-tips", true)
    useJUnitPlatform()
}

jooq {
    version.set(jooqVersion)

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)

            jooqConfiguration.apply {
//                logging = org.jooq.meta.jaxb.Logging.WARN

                jdbc.apply {
                    driver = "org.testcontainers.jdbc.ContainerDatabaseDriver"
                    url = "jdbc:tc:postgresql:11-alpine:///test?TC_INITFUNCTION=com.example.LiquibaseInit::init"
                    user = "test"
                    password = "test"
                }

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "context_schema_jooq"
                        isOutputSchemaToDefault = true
                        includes = ".*"
                        excludes = "databasechangelog.*"

                        forcedTypes.addAll(
                            listOf(
                                org.jooq.meta.jaxb.ForcedType().apply {
                                    userType = "com.example.demojooq.data.enums.StatusEnum"
                                    isEnumConverter = true
                                    includeExpression = """
                                        asdk_context\.status
                                        | asdk_context_history\.status
                                    """.trimIndent()
                                },
                                org.jooq.meta.jaxb.ForcedType().apply {
                                    userType = "com.example.demojooq.data.RuleDto"
                                    isJsonConverter = true
                                    includeExpression = """
                                        asdk_context\.rules
                                        | asdk_context_history\.rules
                                    """.trimIndent()
                                },
//                            org.jooq.meta.jaxb.ForcedType().apply {
//                                userType = "kotlin.collections.Map"
//                                converter = "com.example.demojooq.db.JsonbMapConverter"
//                                includeExpression = "project_rule\\.config"
//                            }
                            )
                        )
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.example.demojooq.jooq.generated"
//                        directory = "build/generated-src/jooq/main"  // default (can be omitted)
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    javaExecSpec = Action {
        systemProperty("org.jooq.no-logo", true)
        systemProperty("org.jooq.no-tips", true)
    }
}
