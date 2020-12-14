package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.distribution.Version;
import ru.yandex.qatools.embed.postgresql.Command;

import java.io.IOException;

import static ru.yandex.qatools.embed.postgresql.distribution.PostgreSQLVersion.Main.PRODUCTION;

/**
 * Configuration for postgres
 */
public class PostgresConfig extends AbstractPostgresConfig<PostgresConfig> {

    public static final long STOP_TIMEOUT_IN_MILLIS = 30000L;

    public PostgresConfig(AbstractPostgresConfig<PostgresConfig> config) {
        super(config);
    }

    public PostgresConfig(AbstractPostgresConfig<PostgresConfig> config, Command command) {
        super(config, command);
    }

    public PostgresConfig(Version version, Net network, Storage storage, Timeout timeout) {
        super(version, network, storage, timeout);
    }

    public PostgresConfig(Version version, Net network, Storage storage, Timeout timeout, Credentials cred, Command command, Long stopTimeoutInMillis) {
        super(version, network, storage, timeout, cred, new PostgresSupportConfig(command), stopTimeoutInMillis);
    }

    public PostgresConfig(Version version, String dbName) throws IOException {
        this(version, new Net(), new Storage(dbName), new Timeout());
    }

    public PostgresConfig(Version version, String host, int port, String dbName) throws IOException {
        this(version, new Net(host, port), new Storage(dbName), new Timeout());
    }

    public PostgresConfig(Version version, Net network, Storage storage, Timeout timeout, Credentials cred, Long stopTimeoutInMillis) {
        this(version, network, storage, timeout, cred, Command.Postgres, stopTimeoutInMillis);
    }

    public static PostgresConfig defaultWithDbName(String dbName, String user, String password) throws IOException {
        return new PostgresConfig(PRODUCTION, new Net(), new Storage(dbName), new Timeout(),
                new Credentials(user, password), STOP_TIMEOUT_IN_MILLIS);
    }

    public static PostgresConfig defaultWithDbName(String dbName) throws IOException {
        return new PostgresConfig(PRODUCTION, dbName);
    }
}
