package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.config.store.*;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.ProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.PackagePaths;
import ru.yandex.qatools.embed.postgresql.ext.SubdirTempDir;


/**
 * Download config builder for postgres
 */
public class PostgresDownloadConfigBuilder extends DownloadConfigBuilder {
    private static final TypedProperty<UserAgent> USER_AGENT = TypedProperty.with("UserAgent", UserAgent.class);
    private static final TypedProperty<ProgressListener> PROGRESS_LISTENER = TypedProperty.with("ProgressListener", ProgressListener.class);
    private static final TypedProperty<ITempNaming> FILE_NAMING = TypedProperty.with("FileNaming", ITempNaming.class);
    private static final TypedProperty<Directory> ARTIFACT_STORE_PATH = TypedProperty.with("ArtifactStorePath", Directory.class);
    private static final TypedProperty<PackageResolver> PACKAGE_RESOLVER = TypedProperty.with("PackageResolver", PackageResolver.class);
    private static final TypedProperty<DownloadPrefix> DOWNLOAD_PREFIX = TypedProperty.with("DownloadPrefix", DownloadPrefix.class);
    private static final TypedProperty<DistributionDownloadPath> DOWNLOAD_PATH = TypedProperty.with("DownloadPath", DistributionDownloadPath.class);

    private static final TypedProperty<TimeoutConfig> TIMEOUT_CONFIG = TypedProperty.with("TimeoutConfig", TimeoutConfig.class);
    private static final TypedProperty<ProxyFactory> PROXY_FACTORY = TypedProperty.with("ProxyFactory", ProxyFactory.class);

    public PostgresDownloadConfigBuilder defaultsForCommand(Command command) {
        fileNaming().setDefault(new UUIDTempNaming());
        // I've found the only open and easy to use cross platform binaries
        downloadPath().setDefault(new SameDownloadPathForEveryDistribution ("http://get.enterprisedb.com/postgresql/"));
        packageResolver().setDefault(new PackagePaths(command, SubdirTempDir.defaultInstance()));
        artifactStorePath().setDefault(new UserHome(".embedpostgresql"));
        downloadPrefix().setDefault(new DownloadPrefix("postgresql-download"));
        userAgent().setDefault(new UserAgent("Mozilla/5.0 (compatible; Embedded postgres; +https://github.com/yandex-qatools)"));
        progressListener().setDefault(new StandardConsoleProgressListener() {
            @Override
            public void info(String label, String message) {
                if (label.startsWith("Extract")) {
                    System.out.print(".");//NOSONAR
                } else {
                    super.info(label, message);//NOSONAR
                }
            }
        });
        return this;
    }

    @Override
    public DownloadConfig build() {
        final DistributionDownloadPath downloadPath = get(DOWNLOAD_PATH);
        final String downloadPrefix = get(DOWNLOAD_PREFIX).value();
        final PackageResolver packageResolver = get(PACKAGE_RESOLVER);
        final Directory artifactStorePath = get(ARTIFACT_STORE_PATH);
        final ITempNaming fileNaming = get(FILE_NAMING);
        final ProgressListener progressListener = get(PROGRESS_LISTENER);
        final String userAgent = get(USER_AGENT).value();
        final TimeoutConfig timeoutConfig = get(TIMEOUT_CONFIG);
        final ProxyFactory proxyFactory = get(PROXY_FACTORY, null);

        return new MutableDownloadConfig(downloadPath, downloadPrefix, packageResolver, artifactStorePath, fileNaming,
                progressListener, userAgent, timeoutConfig, proxyFactory);
    }

    @Override
    public DownloadConfigBuilder downloadPath(String path) {
        set(DOWNLOAD_PATH, new SameDownloadPathForEveryDistribution (path));
        return this;
    }

    @Override
    protected IProperty<DistributionDownloadPath> downloadPath() {
        return property(DOWNLOAD_PATH);
    }

    @Override
    public DownloadConfigBuilder downloadPrefix(String prefix) {
        set(DOWNLOAD_PREFIX, new DownloadPrefix(prefix));
        return this;
    }

    @Override
    protected IProperty<DownloadPrefix> downloadPrefix() {
        return property(DOWNLOAD_PREFIX);
    }

    @Override
    public DownloadConfigBuilder packageResolver(PackageResolver packageResolver) {
        set(PACKAGE_RESOLVER, packageResolver);
        return this;
    }

    @Override
    protected IProperty<PackageResolver> packageResolver() {
        return property(PACKAGE_RESOLVER);
    }

    @Override
    public DownloadConfigBuilder artifactStorePath(Directory artifactStorePath) {
        set(ARTIFACT_STORE_PATH, artifactStorePath);
        return this;
    }

    @Override
    protected IProperty<Directory> artifactStorePath() {
        return property(ARTIFACT_STORE_PATH);
    }

    @Override
    public DownloadConfigBuilder fileNaming(ITempNaming fileNaming) {
        set(FILE_NAMING, fileNaming);
        return this;
    }

    @Override
    protected IProperty<ITempNaming> fileNaming() {
        return property(FILE_NAMING);
    }

    @Override
    public DownloadConfigBuilder progressListener(ProgressListener progressListener) {
        set(PROGRESS_LISTENER, progressListener);
        return this;
    }

    @Override
    protected IProperty<ProgressListener> progressListener() {
        return property(PROGRESS_LISTENER);
    }

    @Override
    public DownloadConfigBuilder userAgent(String userAgent) {
        set(USER_AGENT, new UserAgent(userAgent));
        return this;
    }

    @Override
    protected IProperty<UserAgent> userAgent() {
        return property(USER_AGENT);
    }

    @Override
    public DownloadConfigBuilder timeoutConfig(TimeoutConfig timeoutConfig) {
        set(TIMEOUT_CONFIG, timeoutConfig);
        return this;
    }

    @Override
    protected IProperty<TimeoutConfig> timeoutConfig() {
        return property(TIMEOUT_CONFIG);
    }

    @Override
    public DownloadConfigBuilder proxyFactory(ProxyFactory proxyFactory) {
        set(PROXY_FACTORY, proxyFactory);
        return this;
    }

    @Override
    protected IProperty<ProxyFactory> proxyFactory() {
        return property(PROXY_FACTORY);
    }
}
