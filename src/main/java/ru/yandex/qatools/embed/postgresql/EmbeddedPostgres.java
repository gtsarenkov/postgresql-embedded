package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.runtime.CommandLinePostProcessor;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.embed.postgresql.config.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static ru.yandex.qatools.embed.postgresql.distribution.PostgreSQLVersion.Main.PRODUCTION;
import static ru.yandex.qatools.embed.postgresql.util.SocketUtil.findFreePort;

/**
 * Helper class simplifying the start up configuration for embedded postgres
 */
public class EmbeddedPostgres implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger (EmbeddedPostgres.class);
    public static final String DEFAULT_USER = "postgres";//NOSONAR
    public static final String DEFAULT_PASSWORD = "postgres";//NOSONAR
    public static final String DEFAULT_DB_NAME = "postgres";//NOSONAR
    public static final String DEFAULT_HOST = "localhost";
    private static final List<String> DEFAULT_ADD_PARAMS = asList(
            "-E", "SQL_ASCII",
            "--locale=C",
            "--lc-collate=C",
            "--lc-ctype=C");
    private static final List<String> DEFAULT_POSTGRES_PARAMS = Collections.emptyList ();
    private static final Long DEFAULT_POSTGRES_STOP_TIMEOUT = 60000L;
    private final String dataDir;
    private final Version version;
    private PostgresProcess process;
    private PostgresConfig config;

    public EmbeddedPostgres() {
        this(PRODUCTION);
    }

    public EmbeddedPostgres(Version version) {
        this(version, null);
    }

    public EmbeddedPostgres(String dataDir){
        this(PRODUCTION, dataDir);
    }

    public EmbeddedPostgres(Version version, String dataDir){
        this.version = version;
        this.dataDir = dataDir;
    }

    /**
     * Initializes the default runtime configuration using the temporary directory.
     *
     * @return runtime configuration required for postgres to start.
     */
    public static RuntimeConfig defaultRuntimeConfig() {
        return new RuntimeConfigBuilder()
                .defaults(Command.Postgres)
                .artifactStore(new PostgresArtifactStoreBuilder()
                        .defaults(Command.Postgres).build(builder -> builder
                        .downloadConfig(new PostgresDownloadConfigBuilder()
                                .defaultsForCommand(Command.Postgres)
                                            .progressListener (new Slf4jProgressListener(logger, Slf4jLevel.TRACE))
                                .build())
                        .build()))
                .commandLinePostProcessor(privilegedWindowsRunasPostprocessor())
                .build();
    }

    private static CommandLinePostProcessor privilegedWindowsRunasPostprocessor() {
        if (Platform.detect().operatingSystem () == OS.Windows) {
            try {
                // Based on https://stackoverflow.com/a/11995662
                final int adminCommandResult = Runtime.getRuntime().exec("net session").waitFor();
                if (adminCommandResult == 0) {
                    return runWithoutPrivileges();
                }
            } catch (Exception e) {
                // Log maybe?
            }
        }
        return doNothing();
    }

    private static CommandLinePostProcessor runWithoutPrivileges() {
        return (distribution, args) -> {
            if (args.size() > 0 && args.get(0).endsWith("postgres.exe")) {
                return Arrays.asList("runas", "/trustlevel:0x20000", String.format("\"%s\"", String.join(" ", args)));
            }
            return args;
        };
    }

    private static CommandLinePostProcessor doNothing() {
        return (distribution, args) -> args;
    }

    /**
     * Initializes runtime configuration for cached directory.
     * If a provided directory is empty, postgres will be extracted into it.
     *
     * @param cachedPath path where postgres is supposed to be extracted
     * @return runtime configuration required for postgres to start
     */
    public static RuntimeConfig cachedRuntimeConfig(Path cachedPath) {
        final Command cmd = Command.Postgres;
        final FixedPath cachedDir = new FixedPath(cachedPath.toString());
        return new RuntimeConfigBuilder()
                .defaults(cmd)
                .artifactStore(new PostgresArtifactStoreBuilder()
                        .defaults(cmd).build(builder -> builder
                        .tempDirFactory(cachedDir)
                        .downloadConfig(new PostgresDownloadConfigBuilder()
                                .defaultsForCommand(cmd)
                                .packageResolver(new PackagePaths(cmd, cachedDir))
                                .build())
                        .build()))
                .commandLinePostProcessor(privilegedWindowsRunasPostprocessor())
                .build();
    }

    public String start() throws IOException {
        return start(DEFAULT_HOST, findFreePort(), DEFAULT_DB_NAME);
    }

    public String start(String host, int port, String dbName) throws IOException {
        return start(host, port, dbName, DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_ADD_PARAMS);
    }

    public String start(String host, int port, String dbName, String user, String password) throws IOException {
        return start(defaultRuntimeConfig(), host, port, dbName, user, password, DEFAULT_ADD_PARAMS);
    }

    public String start(String host, int port, String dbName, String user, String password, List<String> additionalParams) throws IOException {
        return start(defaultRuntimeConfig(), host, port, dbName, user, password, additionalParams);
    }

    public String start(RuntimeConfig runtimeConfig) throws IOException {
        return start(runtimeConfig, DEFAULT_HOST, findFreePort(), DEFAULT_DB_NAME, DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_ADD_PARAMS);
    }

    /**
     * Starts up the embedded postgres
     *
     * @param runtimeConfig    required runtime configuration
     * @param host             host to bind to
     * @param port             port to bind to
     * @param dbName           name of the database to initialize
     * @param user             username to connect
     * @param password         password for the provided username
     * @param additionalParams additional database init params (if required)
     * @return connection url for the initialized postgres instance
     * @throws IOException if an I/O error occurs during the process startup
     */
    public String start(RuntimeConfig runtimeConfig, String host, int port, String dbName, String user, String password,
                        List<String> additionalParams) throws IOException {
        return start(runtimeConfig, host, port, dbName, user, password, additionalParams, DEFAULT_POSTGRES_PARAMS, DEFAULT_POSTGRES_STOP_TIMEOUT);
    }

    /**
     * Starts up the embedded postgres
     *
     * @param runtimeConfig    required runtime configuration
     * @param host             host to bind to
     * @param port             port to bind to
     * @param dbName           name of the database to initialize
     * @param user             username to connect
     * @param password         password for the provided username
     * @param additionalInitDbParams additional database init params (if required)
     * @param additionalPostgresParams additional postgresql params (if required)
     * @return connection url for the initialized postgres instance
     * @throws IOException if an I/O error occurs during the process startup
     */
    public String start(RuntimeConfig runtimeConfig, String host, int port, String dbName, String user, String password,
                        List<String> additionalInitDbParams, List<String> additionalPostgresParams, Long stopTimeoutInMillis) throws IOException {
        final PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getInstance(runtimeConfig);
        config = new PostgresConfig(version,
                new AbstractPostgresConfig.Net(host, port),
                new AbstractPostgresConfig.Storage(dbName, dataDir),
                new AbstractPostgresConfig.Timeout(),
                new AbstractPostgresConfig.Credentials(user, password),
                stopTimeoutInMillis
        );
        config.getAdditionalInitDbParams().addAll(additionalInitDbParams);
        config.getAdditionalPostgresParams().addAll(additionalPostgresParams);
        PostgresExecutable exec = runtime.prepare(config);
        this.process = exec.start();
        return formatConnUrl(config);
    }

    /**
     * Returns the configuration of started process
     *
     * @return empty if process has not been started yet
     */
    public Optional<PostgresConfig> getConfig() {
        return ofNullable(config);
    }


    /**
     * Returns the process if started
     *
     * @return empty if process has not been started yet
     */
    public Optional<PostgresProcess> getProcess() {
        return ofNullable(process);
    }

    /**
     * Returns the connection url for the running postgres instance
     *
     * @return empty if process has not been started yet
     */
    public Optional<String> getConnectionUrl() {
        return getConfig().map(this::formatConnUrl);
    }

    private String formatConnUrl(PostgresConfig config) {
        return format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s",//NOSONAR
                config.net().host(),
                config.net().port(),
                config.storage().dbName(),
                config.credentials().username(),
                config.credentials().password()
        );
    }

    public void stop() {
        getProcess().orElseThrow(() -> new IllegalStateException("Cannot stop not started instance!")).stop();
    }

    @Override
    public void close() {
        this.stop();
    }
}
