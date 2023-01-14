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

            new Liquibase("init.sql", new ClassLoaderResourceAccessor(), database)
                .update();

            database.setDefaultSchemaName("project_schema");
            new Liquibase("db/changelog/changelog-master.xml", new ClassLoaderResourceAccessor(), database)
                .update();
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }
}
