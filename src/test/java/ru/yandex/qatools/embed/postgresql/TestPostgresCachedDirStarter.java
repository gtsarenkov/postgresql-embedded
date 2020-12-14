package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import ru.yandex.qatools.embed.postgresql.config.PostgresDownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;

import java.io.File;

import static org.apache.commons.io.FileUtils.getTempDirectory;

public class TestPostgresCachedDirStarter extends TestPostgresStarter {

    @Override
    protected RuntimeConfig buildRuntimeConfig() {
        // turns off the default functionality of unzipping on every run.
        final String tmpDir = new File(getTempDirectory(), "pgembed").getPath();
        final Command cmd = Command.Postgres;
        final FixedPath cachedDir = new FixedPath(tmpDir);
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
                .build();
    }
}
