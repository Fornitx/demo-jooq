package com.example;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class LiquibaseInit {
    public static void init(Connection connection) throws SQLException {
        try {
            var database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            new Liquibase("init-for-jooq-codegen.sql", new ClassLoaderResourceAccessor(), database)
                .update();

            database.setDefaultSchemaName("context_schema_jooq");
            new Liquibase("db/changelog/changelog-master.xml", new ClassLoaderResourceAccessor(), database)
                .update();
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }
}
