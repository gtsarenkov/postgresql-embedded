package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.PackageResolver;

public interface IMutableDownloadConfig extends DownloadConfig {
    void setPackageResolver(PackageResolver packageResolver);
}
