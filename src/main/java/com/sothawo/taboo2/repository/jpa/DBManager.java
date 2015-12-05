package com.sothawo.taboo2.repository.jpa;

import com.sothawo.taboo2.TabooException;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class to manage a jdbc database with liquibase. Utility class.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
public final class DBManager {
// -------------------------- STATIC METHODS --------------------------

    /**
     * run a liquibase changelog on a database
     *
     * @param jdbcUrl
     *         database url
     * @param changeLogResource
     *         changelog resource
     */
    public static void updateDB(String jdbcUrl, String changeLogResource) {
        try {
            DatabaseConnection dbConnection = new JdbcConnection(DriverManager.getConnection(jdbcUrl));

            Liquibase liquibase =
                    new Liquibase(changeLogResource, new ClassLoaderResourceAccessor(), dbConnection);
            // as we are the only process using the database, we can do a release locks safely in case the last
            // instance got stuck.
            liquibase.forceReleaseLocks();
            liquibase.update(new Contexts());
        } catch (SQLException | LiquibaseException e) {
            throw new TabooException("update database", e);
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private DBManager() {
    }
}
