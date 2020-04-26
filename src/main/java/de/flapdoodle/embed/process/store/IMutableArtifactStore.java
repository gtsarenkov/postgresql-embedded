package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.config.store.DownloadConfig;

public interface IMutableArtifactStore extends IArtifactStore {
    void setDownloadConfig(DownloadConfig downloadConfig);
}
