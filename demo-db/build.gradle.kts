plugins {
    id("java")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${System.getProperty("springVersion")}"))
    implementation("org.liquibase:liquibase-core")
}
