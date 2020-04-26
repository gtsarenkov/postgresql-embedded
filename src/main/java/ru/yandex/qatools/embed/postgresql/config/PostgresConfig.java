package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.distribution.Version;
import ru.yandex.qatools.embed.postgresql.Command;

import java.io.IOException;

import static ru.yandex.qatools.embed.postgresql.distribution.PostgreSQLVersion.Main.PRODUCTION;

/**
 * Configuration for postgres
 */
public class PostgresConfig extends AbstractPostgresConfig<PostgresConfig> {

    public PostgresConfig(AbstractPostgresConfig config, Command command) {
        super(config, command);
    }

    public PostgresConfig(AbstractPostgresConfig config) {
        super(config);
    }

    public PostgresConfig(Version version, String dbName) throws IOException {
        this(version, new Net(), new Storage(dbName), new Timeout());
    }

    public PostgresConfig(Version version, String host, int port, String dbName) throws IOException {
        this(version, new Net(host, port), new Storage(dbName), new Timeout());
    }

    public PostgresConfig(Version version, Net network, Storage storage, Timeout timeout, Credentials cred, Command command) {
        super(version, network, storage, timeout, cred, new SupportConfig(command));
    }

    public PostgresConfig(Version version, Net network, Storage storage, Timeout timeout, Credentials cred) {
        this(version, network, storage, timeout, cred, Command.Postgres);
    }

    public PostgresConfig(Version version, Net network, Storage storage, Timeout timeout) {
        super(version, network, storage, timeout);
    }

    public static PostgresConfig defaultWithDbName(String dbName, String user, String password) throws IOException {
        return new PostgresConfig(PRODUCTION, new Net(), new Storage(dbName), new Timeout(),
                new Credentials(user, password));
    }

    public static PostgresConfig defaultWithDbName(String dbName) throws IOException {
        return new PostgresConfig(PRODUCTION, dbName);
    }
}
