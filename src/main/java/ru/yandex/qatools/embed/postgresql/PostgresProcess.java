package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.process.ProcessOutput;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.ImmutableDownloadConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.Slf4jStreamProcessor;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.runtime.Executable;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.store.ArtifactStore;
import de.flapdoodle.embed.process.store.ImmutableArtifactStore;
import de.flapdoodle.embed.process.store.PostgresArtifactStore;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;
import ru.yandex.qatools.embed.postgresql.ext.SubdirTempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static de.flapdoodle.embed.process.io.file.Files.forceDelete;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.apache.commons.io.FileUtils.readLines;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;
import static ru.yandex.qatools.embed.postgresql.Command.*;
import static ru.yandex.qatools.embed.postgresql.PostgresStarter.getCommand;
import static ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Storage;

/**
 * postgres process
 */
public class PostgresProcess extends AbstractPGProcess<PostgresExecutable, PostgresProcess> {
    private static final int MAX_CREATEDB_TRIALS = 3;
    private static final int DEFAULT_CMD_TIMEOUT = 2000;
    private static final Logger LOGGER = getLogger(PostgresProcess.class);
    private final RuntimeConfig runtimeConfig;

    private volatile boolean processReady = false;
    private volatile boolean stopped = false;

    public PostgresProcess(Distribution distribution, PostgresConfig config,
                           RuntimeConfig runtimeConfig, PostgresExecutable executable) throws IOException {
        super(distribution, config, runtimeConfig, executable);
        this.runtimeConfig = runtimeConfig;
    }

    private static String runCmd(PostgresConfig config, RuntimeConfig parentRuntimeCfg, Command cmd, String successOutput,
                                 Set<String> failOutput, String... args) {
        return runCmd(false, config, parentRuntimeCfg, cmd, successOutput, failOutput, args);
    }

    private static String runCmd(boolean silent,
                                 PostgresConfig config, RuntimeConfig parentRuntimeCfg, Command cmd, String successOutput,
                                 Set<String> failOutput, String... args) {
        try {
            final Slf4jStreamProcessor destination = new Slf4jStreamProcessor (LOGGER, Slf4jLevel.TRACE);
            final LogWatchStreamProcessor logWatch = new LogWatchStreamProcessor(successOutput, failOutput, destination);

            final PostgresArtifactStore artifactStore = (PostgresArtifactStore) parentRuntimeCfg.artifactStore();
            final PostgresArtifactStoreBuilder artifactStoreBuilder = new PostgresArtifactStoreBuilder(ImmutableArtifactStore.builder().from(artifactStore));

            DownloadConfig downloadCfg = artifactStore.downloadConfig();
            final ImmutableDownloadConfig.Builder builderDownloadConfig = ImmutableDownloadConfig.builder().from(downloadCfg);

            Directory tempDir = SubdirTempDir.defaultInstance();
            if (downloadCfg.getPackageResolver() instanceof PackagePaths) {
                tempDir = ((PackagePaths) downloadCfg.getPackageResolver()).getTempDir();
            }
            builderDownloadConfig.packageResolver(new PackagePaths(cmd, tempDir));
            final ArtifactStore newArtifactoryStore = artifactStoreBuilder.build (builder -> builder.downloadConfig(builderDownloadConfig.build ()).build ());

            final RuntimeConfig runtimeCfg = new RuntimeConfigBuilder().defaults(cmd)
                    .isDaemonProcess(false)
                    .processOutput(ProcessOutput.builder().output(logWatch).error(logWatch).commands(logWatch).build())
                    .artifactStore(newArtifactoryStore)
                    .commandLinePostProcessor(parentRuntimeCfg.commandLinePostProcessor()).build();

            final PostgresConfig postgresConfig = new PostgresConfig(config).withArgs(args);
            final Executable<?, ? extends AbstractPGProcess> exec = getCommand(cmd, runtimeCfg)
                    .prepare(postgresConfig);
            AbstractPGProcess proc = exec.start();
            CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
                try {
                    return proc.waitFor();
                }
                catch(InterruptedException e) {
                    LOGGER.warn("Command {} interrupted", cmd);
                    throw new RuntimeException("Command {%s} interrupted".formatted(cmd), e);
                }
            }).whenComplete((Integer exitCode, Throwable error) -> {
                if (exitCode != 0) {
                    LOGGER.warn("Exit code {}: {}({})", cmd, exitCode, Integer.toHexString (exitCode), error);
                }
            });
            String output;
            boolean initWithSuccess;
            String failureFound;
            do {
                initWithSuccess = logWatch.isInitWithSuccess();
                failureFound = logWatch.getFailureFound();
                if (!initWithSuccess && Objects.isNull(failureFound)) {
                    logWatch.waitForResult(DEFAULT_CMD_TIMEOUT);
                    initWithSuccess = logWatch.isInitWithSuccess();
                    failureFound = logWatch.getFailureFound();
                    LOGGER.info("Caught output: {} {}", initWithSuccess, failureFound);
                }
                output = logWatch.getOutput();
                if (f2.isDone() && proc.isProcessRunning()) {
                    LOGGER.warn("Process {} waiting finished but it is still running", cmd);
                }
                if (!"<EOF>".equals(Objects.requireNonNullElse(failureFound, "<EOF>"))) {
                    break;
                }
            } while (proc.isProcessRunning() && !initWithSuccess && Objects.isNull(failureFound));
            if (!initWithSuccess && !silent) {
                LOGGER.warn("Possibly failed to run {} {}:\n{}", cmd.commandName(), failureFound, output);
            }
            return output;
        } catch (IOException e) {
            if (!silent) {
                LOGGER.warn("Failed to run command {}", cmd, e);
            }
        }
        return null;
    }

    private static boolean shutdownPostgres(PostgresConfig config, RuntimeConfig runtimeConfig) {
        try {
            return isEmpty(runCmd(true, config, runtimeConfig, Command.PgCtl, "server stopped", emptySet(), "stop"));
        } catch (Exception e) {
            LOGGER.trace("Failed to stop postgres by pg_ctl!", e);
        }
        return false;
    }

    @Override
    protected synchronized void stopInternal() {
        if (!stopped) {
            try {
              stopped = !isProcessRunning();
            }
            catch (NullPointerException ignored) {
            }
            catch (Throwable e) {
              LOGGER.error("Cannot stop postgres", e);
              throw e;
            }
            if (!stopped) {
                stopped = false;
                LOGGER.info("trying to stop postgresql");
                if (!sendStopToPostgresqlInstance() && !sendTermToProcess() && waitUntilProcessHasStopped(2000)) {
                    LOGGER.warn("could not stop postgresql with pg_ctl/SIGTERM, trying to kill it...");
                    if (!sendKillToProcess() && !tryKillToProcess() && waitUntilProcessHasStopped(3000)) {
                        LOGGER.warn("could not kill postgresql within 4s!");
                    }
                }
            }
        }
        if (waitUntilProcessHasStopped(5000)) {
            LOGGER.error("Postgres has not been stopped within 10s! Something's wrong!");
        }
        deleteTempFiles();
    }

    //TODO: investigate use of stop timeout.
    private boolean waitUntilProcessHasStopped(int timeoutMillis) {
        long started = currentTimeMillis();
        while (currentTimeMillis() - started < timeoutMillis && isProcessRunning()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                LOGGER.warn("Failed to wait with timeout until the process has been killed", e);
            }
        }
        return isProcessRunning();
    }

    protected final boolean sendStopToPostgresqlInstance() {
        final boolean result = shutdownPostgres(getConfig(), runtimeConfig);
        if (runtimeConfig.artifactStore() instanceof PostgresArtifactStore) {
            final Directory tempDir = ((PostgresArtifactStore) runtimeConfig.artifactStore()).tempDirFactory();
            if (tempDir != null && tempDir.asFile() != null && tempDir.isGenerated()) {
                LOGGER.info("Cleaning up after the embedded process (removing {})...", tempDir.asFile().getAbsolutePath());
                forceDelete(tempDir.asFile());
            }
        }
        return result;
    }

    @Override
    protected void onBeforeProcess(RuntimeConfig runtimeConfig) {
        super.onBeforeProcess(runtimeConfig);
        PostgresConfig config = getConfig();

        final File     dbDir   = config.storage().dbDir();
        final File[] dbFiles = dbDir.listFiles();
        if (dbDir.exists() && dbFiles != null && dbFiles.length > 0) {
            return;
        }

        runCmd(config, runtimeConfig, InitDb, "Success", singleton("[initdb error]"));
    }

    @Override
    protected List<String> getCommandLine(Distribution distribution, PostgresConfig config, ExtractedFileSet exe) {
        List<String> ret = new ArrayList<>();
        switch (config.supportConfig().name()) {
            case "postgres": //NOSONAR
                ret.addAll(asList(exe.executable().getAbsolutePath(),
                        "-p", String.valueOf(config.net().port()),
                        "-h", config.net().host(),
                        "-D", config.storage().dbDir().getAbsolutePath()
                ));
                ret.addAll(config.getAdditionalPostgresParams());
                break;
            case "pg_ctl": //NOSONAR
                ret.addAll(asList(exe.executable().getAbsolutePath(),
                        "-o",
                        String.format("\"-p %s -h %s %s\"", config.net().port(), config.net().host(), String.join (" ", config.getAdditionalPostgresParams())),
                        "-D", config.storage().dbDir().getAbsolutePath(),
                        "-w",
                        "start"
                ));
                break;
            default:
                throw new RuntimeException("Failed to launch Postgres: Unknown command " +
                        config.supportConfig().name() + "!");
        }
        return ret;
    }

    protected void deleteTempFiles() {
        final Storage storage = getConfig().storage();
        if (storage.dbDir() == null) {
            return;
        }
        if (!storage.isTmpDir()) {
            return;
        }

        if (!forceDelete(storage.dbDir())) {
            LOGGER.warn("Could not delete temp db dir: {}", storage.dbDir());
        }
    }

    @Override
    protected final void onAfterProcessStart(ProcessControl process,
                                             RuntimeConfig runtimeConfig) {
        final Storage storage     = getConfig().storage();
        final Path    pidFilePath = Paths.get(storage.dbDir().getAbsolutePath(), "postmaster.pid");
        final File    pidFile     = new File(pidFilePath.toAbsolutePath().toString());
        int           timeout     = TIMEOUT;
        while (!pidFile.exists() && ((timeout = timeout - 100) > 0)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) { /* safe to ignore */ }
        }
        int pid = -1;
        try {
            pid = Integer.valueOf(readLines(pidFilePath.toFile()).get(0));
        } catch (Exception e) {
            LOGGER.error("Failed to read PID file ({})", e.getMessage(), e);
        }
        if (pid != -1) {
            setProcessId(pid);
        } else {
            // fallback, try to read pid file. will throw IOException if that fails
          try {
            setProcessId(getPidFromFile(pidFile()));
          }
          catch (IOException e) {
            throw new IllegalStateException (String.format("Unable to determine pid from %s", pidFile()), e);
          }
        }

        int trial = 0;
        do {
            String output = runCmd(getConfig(),
                                   runtimeConfig,
                                   CreateDb,
                                   "",
                                   new HashSet<>(singleton("database creation failed")),
                                   storage.dbName());
            try {
                if (isEmpty(output) || !output.contains("could not connect to database")) {
                    this.processReady = true;
                    break;
                }
                LOGGER.warn("Could not create database first time ({} of {} trials)", trial, MAX_CREATEDB_TRIALS);
                sleep(100);
            } catch (InterruptedException ie) { /* safe to ignore */ }
        } while (trial++ < MAX_CREATEDB_TRIALS);
    }

    /**
     * Import into database from file
     *
     * @param file The file to import into database
     */
    public void importFromFile(File file) {
        importFromFileWithArgs(file);
    }

    /**
     * Import into database from file with additional args
     *
     * @param file    file to import
     * @param cliArgs additional arguments for psql (be sure to separate args from their values)
     */
    public void importFromFileWithArgs(File file, String... cliArgs) {
        if (file.exists()) {
            String[] args = {
                    "-U", getConfig().credentials().username(),
                    "-d", getConfig().storage().dbName(),
                    "-h", getConfig().net().host(),
                    "-p", String.valueOf(getConfig().net().port()),
                    "-f", file.getAbsolutePath()};
            if (cliArgs != null && cliArgs.length != 0) {
                args = ArrayUtils.addAll(args, cliArgs);
            }
            runCmd(getConfig(), runtimeConfig, Psql, "", new HashSet<>(singletonList("import into " + getConfig().storage().dbName() + " failed")), args);
        }
    }

    /**
     * Import into database from file with additional args
     *
     * @param file    file to restore
     * @param cliArgs additional arguments for psql (be sure to separate args from their values)
     */
    public void restoreFromFile(File file, String... cliArgs) {
        if (file.exists()) {
            String[] args = {
                    "-U", getConfig().credentials().username(),
                    "-d", getConfig().storage().dbName(),
                    "-h", getConfig().net().host(),
                    "-p", String.valueOf(getConfig().net().port()),
                    file.getAbsolutePath()};
            if (cliArgs != null && cliArgs.length != 0) {
                args = ArrayUtils.addAll(args, cliArgs);
            }
            runCmd(getConfig(), runtimeConfig, PgRestore, "", new HashSet<>(singletonList("restore into " + getConfig().storage().dbName() + " failed")), args);
        }
    }

    public void exportToFile(File file) {
        runCmd(getConfig(), runtimeConfig, PgDump, "", new HashSet<>(singletonList("export from " + getConfig().storage().dbName() + " failed")),
                "-U", getConfig().credentials().username(),
                "-d", getConfig().storage().dbName(),
                "-h", getConfig().net().host(),
                "-p", String.valueOf(getConfig().net().port()),
                "-f", file.getAbsolutePath()
        );
    }

    public void exportSchemeToFile(File file) {
        runCmd(getConfig(), runtimeConfig, PgDump, "", new HashSet<>(singletonList("export from " + getConfig().storage().dbName() + " failed")),
                "-U", getConfig().credentials().username(),
                "-d", getConfig().storage().dbName(),
                "-h", getConfig().net().host(),
                "-p", String.valueOf(getConfig().net().port()),
                "-f", file.getAbsolutePath(),
                "-s"
        );
    }

    public void exportDataToFile(File file) {
        runCmd(getConfig(), runtimeConfig, PgDump, "", new HashSet<>(singletonList("export from " + getConfig().storage().dbName() + " failed")),
                "-U", getConfig().credentials().username(),
                "-d", getConfig().storage().dbName(),
                "-h", getConfig().net().host(),
                "-p", String.valueOf(getConfig().net().port()),
                "-f", file.getAbsolutePath(),
                "-a"
        );
    }

    public boolean isProcessReady() {
        return processReady;
    }

    @Override
    protected void cleanupInternal() {
    }
}
