package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.config.store.DistributionDownloadPath;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.config.store.ProxyFactory;
import de.flapdoodle.embed.process.config.store.TimeoutConfig;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.progress.ProgressListener;

import java.util.Optional;

public class MutableDownloadConfig implements IMutableDownloadConfig {

    private final DistributionDownloadPath downloadPath;
    private final ProgressListener progressListener;
    private final Directory artifactStorePath;
    private final ITempNaming fileNaming;
    private final String downloadPrefix;
    private final String userAgent;
    private final TimeoutConfig timeoutConfig;
    private final ProxyFactory proxyFactory;
    private PackageResolver packageResolver;

    public MutableDownloadConfig(DistributionDownloadPath downloadPath, String downloadPrefix, PackageResolver packageResolver,//NOSONAR
                                 Directory artifactStorePath, ITempNaming fileNaming, ProgressListener progressListener, String userAgent,//NOSONAR
                                 TimeoutConfig timeoutConfig, ProxyFactory proxyFactory) { //NOSONAR
        super();
        this.downloadPath = downloadPath;
        this.downloadPrefix = downloadPrefix;
        this.packageResolver = packageResolver;
        this.artifactStorePath = artifactStorePath;
        this.fileNaming = fileNaming;
        this.progressListener = progressListener;
        this.userAgent = userAgent;
        this.timeoutConfig = timeoutConfig;
        this.proxyFactory = proxyFactory;
    }

    @Override
    public DistributionDownloadPath getDownloadPath() {
        return downloadPath;
    }

    @Override
    public ProgressListener getProgressListener() {
        return progressListener;
    }

    @Override
    public Directory getArtifactStorePath() {
        return artifactStorePath;
    }

    @Override
    public ITempNaming getFileNaming() {
        return fileNaming;
    }

    @Override
    public String getDownloadPrefix() {
        return downloadPrefix;
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }

    @Override
    public PackageResolver getPackageResolver() {
        return packageResolver;
    }

    @Override
    public void setPackageResolver(PackageResolver packageResolver) {
        this.packageResolver = packageResolver;
    }

    @Override
    public TimeoutConfig getTimeoutConfig() {
        return timeoutConfig;
    }

    @Override
    public Optional<ProxyFactory> proxyFactory() {
        return Optional.of (proxyFactory);
    }
}