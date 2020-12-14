package de.flapdoodle.embed.process.store;

import java.util.function.Function;

public class NonCachedPostgresArtifactStoreBuilder extends PostgresArtifactStoreBuilder {

    @Override
    public ArtifactStore build(Function<ImmutableArtifactStore.Builder, ImmutableArtifactStore> builder) {
        final ArtifactStore artifactStore = super.build (builder);
        return new PostgresArtifactStore(artifactStore.downloadConfig(), artifactStore.tempDirFactory(), artifactStore.executableNaming(), artifactStore.downloader());
    }
}
