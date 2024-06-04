package ru.yandex.qatools.embed.postgresql;

import org.junit.Test;
import ru.yandex.qatools.embed.postgresql.distribution.PostgreSQLVersion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class TestMultipleInstance {

    public static final String POSTGRE_SQL_PRODUCTION = "PostgreSQL 16.";

    @Test
    public void itShouldAllowToRunTwoInstancesWithDifferentVersions() throws Exception {
        final EmbeddedPostgres postgres0 = new EmbeddedPostgres();
        start(postgres0);
        checkVersion(postgres0, POSTGRE_SQL_PRODUCTION);
        postgres0.stop();

        final EmbeddedPostgres postgres1 = new EmbeddedPostgres(PostgreSQLVersion.Main.V16);
        start(postgres1);
        checkVersion(postgres1, "PostgreSQL 16.");
        postgres1.stop();
    }

    @Test
    public void itShouldAllowToRunTwoInstancesAtSameTime() throws Exception {
        final EmbeddedPostgres postgres0 = new EmbeddedPostgres();
        start(postgres0);

        final EmbeddedPostgres postgres1 = new EmbeddedPostgres();
        start(postgres1);

        checkVersion(postgres0, POSTGRE_SQL_PRODUCTION);
        checkVersion(postgres1, POSTGRE_SQL_PRODUCTION);

        postgres0.stop();
        postgres1.stop();
    }

    @Test
    public void itShouldAllowToRunTwoInstancesAtSameTimeAndWithDifferentVersions() throws Exception {
        final EmbeddedPostgres postgres0 = new EmbeddedPostgres(PostgreSQLVersion.Main.V12);
        start(postgres0);

        final EmbeddedPostgres postgres1 = new EmbeddedPostgres(PostgreSQLVersion.Main.V13);
        start(postgres1);

        checkVersion(postgres0, "PostgreSQL 12.");
        checkVersion(postgres1, "PostgreSQL 13.");

        postgres0.stop();
        postgres1.stop();
    }

    private void start(EmbeddedPostgres postgres) throws Exception {
        postgres.start();
        assertThat(postgres.getConnectionUrl().isPresent(), is(true));
    }

    private void checkVersion(EmbeddedPostgres postgres, String expectedVersion) throws Exception {
        String jdbcUrl = postgres.getConnectionUrl().get();
        try (final Connection conn = DriverManager.getConnection(jdbcUrl);
             final Statement statement = conn.createStatement()) {
            assertThat(statement.execute("SELECT version();"), is(true));
            assertThat(statement.getResultSet().next(), is(true));
            assertThat(statement.getResultSet().getString("version"), containsString(expectedVersion));
        }
    }
}
