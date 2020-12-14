package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.config.PostgresDownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.ext.SubdirTempDir;

import java.util.function.Function;

public class PostgresArtifactStoreBuilder {
    private final ImmutableArtifactStore.Builder builder;

    public PostgresArtifactStoreBuilder() {
        this(ImmutableArtifactStore.builder());
    }

    public PostgresArtifactStoreBuilder (final ImmutableArtifactStore.Builder builder) {
        this.builder = builder;
    }

    public PostgresArtifactStoreBuilder defaults(final Command command) {
        builder.tempDirFactory (new SubdirTempDir())
            .executableNaming(new UUIDTempNaming())
            .downloadConfig (new PostgresDownloadConfigBuilder().defaultsForCommand(command).build())
            .downloader(new UrlConnectionDownloader());
        return this;
    }

    public PostgresArtifactStore build(Function<ImmutableArtifactStore.Builder, ImmutableArtifactStore> builder) {
        final ImmutableArtifactStore artifactStore = builder.apply(this.builder);
        return new CachedPostgresArtifactStore(artifactStore.downloadConfig(), artifactStore.tempDirFactory(), artifactStore.executableNaming(), artifactStore.downloader());
    }

}
