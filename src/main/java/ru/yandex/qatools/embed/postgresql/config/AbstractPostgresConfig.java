package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.config.ExecutableProcessConfig;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.file.Files;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.ext.SubdirTempDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

import static de.flapdoodle.embed.process.runtime.Network.getFreeServerPort;
import static de.flapdoodle.embed.process.runtime.Network.getLocalHost;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Common postgres config
 */
public abstract class AbstractPostgresConfig<C extends AbstractPostgresConfig<?>> implements ExecutableProcessConfig {

    public static final long DEFAULT_STOP_TIMEOUT = -1L;
    private final Storage storage;
    protected final Net network;
    protected final Timeout timeout;
    protected final Credentials credentials;
    private final Version version;
    private final PostgresSupportConfig postgresSupportConfig;
    private final Long stopTimeoutInMillis;
    protected final List<String> args = new ArrayList<>();
    protected final List<String> additionalInitDbParams = new ArrayList<>();
    protected final List<String> additionalPostgresParams = new ArrayList<>();

    protected AbstractPostgresConfig(AbstractPostgresConfig<C> config, Command postgres) {
        // TODO: review default value for timeout -1 or 0 and use INDEFINITE.
        this(config.version(), config.net(), config.storage(), config.timeout(), config.credentials, new PostgresSupportConfig (postgres), config.stopTimeoutInMillis ().orElse (DEFAULT_STOP_TIMEOUT));
        this.additionalInitDbParams.addAll(config.getAdditionalInitDbParams());
        this.additionalPostgresParams.addAll(config.getAdditionalPostgresParams());
    }

    protected AbstractPostgresConfig(AbstractPostgresConfig<C> config) {
        this(config, Command.Postgres);
    }

    public AbstractPostgresConfig(Version version, Net network, Storage storage, Timeout timeout, Credentials cred, PostgresSupportConfig postgresSupportConfig, Long stopTimeoutInMillis) {
        this.version = version;
        this.postgresSupportConfig = postgresSupportConfig;
        this.network = network;
        this.timeout = timeout;
        this.stopTimeoutInMillis = stopTimeoutInMillis;
        this.storage = storage;
        this.credentials = cred;
    }

    public AbstractPostgresConfig(Version version, Net network, Storage storage, Timeout timeout) {
        this(version, network, storage, timeout, null, new PostgresSupportConfig (Command.Postgres), DEFAULT_STOP_TIMEOUT);
    }

    public Net net() {
        return network;
    }

    public Timeout timeout() {
        return timeout;
    }

    public Storage storage() {
        return storage;
    }

    public Credentials credentials() {
        return credentials;
    }

    public List<String> args() {
        return args;
    }

  /**
   * Adds arguments to the list.
   *
   * @implNote need to review unchecked typecast
   * @param args arguments
   * @return C
   */
    @SuppressWarnings ("unchecked")
    public C withArgs(String... args) {
        args().addAll(asList(args));
        // TODO: refactor
        return (C) this;
    }

  /**
   * Adds additional arguments for InitDb.
   *
   * @implNote needed to review unchecked typecast.
   * @param additionalInitDbParams additional parameters
   * @return C
   */
    @SuppressWarnings ("unchecked")
    public C withAdditionalInitDbParams(List<String> additionalInitDbParams) {
        this.additionalInitDbParams.addAll(additionalInitDbParams);
        // TODO: refactor
        return (C) this;
    }

    /**
     * You may add here additional arguments for the {@code initdb} executable.<br/>
     * <p>
     * Example.<br>
     * to support german umlauts you would add here this additional arguments.<br/>
     * <pre>
     * getAdditionalInitDbParams().addAll(
     *      java.util.Arrays.asList(
     *          "-E", "'UTF-8'",
     *          "--lc-collate='de_DE.UTF-8'",
     *          "--lc-ctype=locale='de_DE.UTF-8'")
     * )
     * </pre>
     *
     * @return The list of additional parameters for the {@code initdb} executable.<br/>
     * Not {@code null}.<br/>
     */
    public List<String> getAdditionalInitDbParams() {
        return additionalInitDbParams;
    }

    /**
     * You may add here additional arguments for the {@code postgres} executable.<br/>
     * <p>
     * Example.<br>
     * to use custom number of maximum connection.<br/>
     * <pre>
     * getAdditionalPostgresParams().addAll(
     *      java.util.Arrays.asList(
     *          "-c", "max_connections=11"
     * )
     * </pre>
     * @return The list of additional parameters for the {@code postgres} executable.<br/>
     * Not {@code null}.<br>
     * @see <a href="https://www.postgresql.org/docs/9.6/static/config-setting.html#AEN32659">Parameter Interaction via the Shell</a>
     */
    public List<String> getAdditionalPostgresParams() {
        return additionalPostgresParams;
    }

    @Override
    public Version version () {
        return version;
      }

    @Override
    public SupportConfig supportConfig () {
        return postgresSupportConfig;
      }

    @Override
    public OptionalLong stopTimeoutInMillis () {
        return Objects.isNull(stopTimeoutInMillis)?OptionalLong.empty():OptionalLong.of(stopTimeoutInMillis);
      }

    public static class Storage {
        private final File dbDir;
        private final String dbName;
        private final boolean isTmpDir;

        public Storage(String dbName) throws IOException {
            this(dbName, null);
        }

        public Storage(String dbName, String databaseDir) throws IOException {
            this.dbName = dbName;
            if (isEmpty(databaseDir)) {
                isTmpDir = true;
                dbDir = Files.createTempDir(SubdirTempDir.defaultInstance(), "db-content");
            } else {
                dbDir = Files.createOrCheckDir(databaseDir);
                isTmpDir = false;
            }
        }

        public File dbDir() {
            return dbDir;
        }

        public boolean isTmpDir() {
            return isTmpDir;
        }

        public String dbName() {
            return dbName;
        }

        @Override
        public String toString() {
            return "Storage{" +
                    "dbDir=" + dbDir +
                    ", dbName='" + dbName + '\'' +
                    ", isTmpDir=" + isTmpDir +
                    '}';
        }
    }

    public static class Credentials {

        private final String username;
        private final String password;


        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }

        @Override
        public String toString() {
            return "Credentials{" +
                    "username='" + username + '\'' +
                    ", password='" + password + '\'' + //NOSONAR
                    '}';
        }
    }

    public static class Net {

        private final String host;
        private final int port;

        public Net() throws IOException {
            this(getLocalHost().getHostAddress(), getFreeServerPort());
        }

        public Net(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public int port() {
            return port;
        }

        public String host() {
            return host;
        }

        @Override
        public String toString() {
            return "Net{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    public static class Timeout {

        private final long startupTimeout;

        public Timeout() {
            this(15000);
        }

        public Timeout(long startupTimeout) {
            this.startupTimeout = startupTimeout;
        }

        public long startupTimeout() {
            return startupTimeout;
        }

        @Override
        public String toString() {
            return "Timeout{" +
                    "startupTimeout=" + startupTimeout +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AbstractPostgresConfig{" +
                "storage=" + storage +
                ", network=" + network +
                ", timeout=" + timeout +
                ", credentials=" + credentials +
                ", args=" + args +
                ", additionalInitDbParams=" + additionalInitDbParams +
                '}';
    }
}
