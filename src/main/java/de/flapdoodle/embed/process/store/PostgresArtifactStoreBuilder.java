package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.config.PostgresDownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.ext.SubdirTempDir;

import java.util.function.Function;

public class PostgresArtifactStoreBuilder {
    private ImmutableArtifactStore.Builder builder;

    public PostgresArtifactStoreBuilder defaults(final Command command) {
        builder = ImmutableArtifactStore.builder()
            .tempDirFactory (new SubdirTempDir())
            .executableNaming(new UUIDTempNaming())
            .downloadConfig (new PostgresDownloadConfigBuilder().defaultsForCommand(command).build())
            .downloader(new UrlConnectionDownloader());
        return this;
    }

    public ArtifactStore build(Function<ImmutableArtifactStore.Builder, ImmutableArtifactStore> builder) {
        final ImmutableArtifactStore artifactStore = builder.apply(this.builder);
        return new CachedPostgresArtifactStore(artifactStore.downloadConfig(), artifactStore.tempDirFactory(), artifactStore.executableNaming(), artifactStore.downloader());
    }

}
