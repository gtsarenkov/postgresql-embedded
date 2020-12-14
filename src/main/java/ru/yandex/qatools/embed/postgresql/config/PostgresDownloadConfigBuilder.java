package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.config.store.*;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.PackagePaths;
import ru.yandex.qatools.embed.postgresql.ext.SubdirTempDir;


/**
 * Download config builder for postgres
 */
public class PostgresDownloadConfigBuilder {
    private ImmutableDownloadConfig.Builder builder;

    public ImmutableDownloadConfig.Builder defaultsForCommand(Command command) {
        builder = ImmutableDownloadConfig.builder ()
            .fileNaming (new UUIDTempNaming ())
            // I've found the only open and easy to use cross platform binaries
            .downloadPath (new SameDownloadPathForEveryDistribution ("http://get.enterprisedb.com/postgresql/"))
            .packageResolver (new PackagePaths(command, SubdirTempDir.defaultInstance()))
            .artifactStorePath(new UserHome(".embedpostgresql"))
            .downloadPrefix("postgresql-download")
            .userAgent("Mozilla/5.0 (compatible; Embedded postgres; +https://github.com/yandex-qatools)")
            .progressListener(new StandardConsoleProgressListener() {
            @Override
            public void info(String label, String message) {
                if (label.startsWith("Extract")) {
                    System.out.print(".");//NOSONAR
                } else {
                    super.info(label, message);//NOSONAR
                }
            }
        });
        return builder;
    }

    public DownloadConfig build() {
        final ImmutableDownloadConfig downloadConfig = builder.build ();

        return new MutableDownloadConfig(downloadConfig.getDownloadPath(), downloadConfig.getDownloadPrefix(),
            downloadConfig.getPackageResolver(), downloadConfig.getArtifactStorePath(), downloadConfig.getFileNaming(),
            downloadConfig.getProgressListener(), downloadConfig.getUserAgent(), downloadConfig.getTimeoutConfig(), downloadConfig.proxyFactory().orElse(null));
    }
}
