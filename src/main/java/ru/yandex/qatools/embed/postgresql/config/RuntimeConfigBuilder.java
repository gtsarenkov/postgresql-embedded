package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.config.ImmutableRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.runtime.CommandLinePostProcessor;
import org.slf4j.Logger;
import ru.yandex.qatools.embed.postgresql.Command;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;

/**
 * Configuration builder
 */
public class RuntimeConfigBuilder {

    public ImmutableRuntimeConfig.Builder defaults(Command command) {
        return ImmutableRuntimeConfig.builder()
          .isDaemonProcess(false)
          .processOutput (ProcessOutput.getDefaultInstance(command.commandName()))
          .commandLinePostProcessor (new CommandLinePostProcessor.Noop())
          .artifactStore (storeBuilder().defaults(command).build(builder -> builder.build()));
    }

    public ImmutableRuntimeConfig.Builder defaultsWithLogger(Command command, Logger logger) {
        DownloadConfig downloadConfig = new PostgresDownloadConfigBuilder()
          .defaultsForCommand(command)
          .progressListener(new Slf4jProgressListener(logger))
          .build();
        return defaults(command)
          .processOutput (PostgresProcessOutputConfig.getInstance(command, logger))
          .artifactStore(storeBuilder().defaults(command).build(builder -> builder.downloadConfig(downloadConfig).build()));
    }

    private PostgresArtifactStoreBuilder storeBuilder() {
        return new PostgresArtifactStoreBuilder();
    }

}
