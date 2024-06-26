package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.Extractors;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.extract.Extractor;
import de.flapdoodle.embed.process.extract.TempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.apache.commons.io.FileUtils.deleteQuietly;

/**
 * @author Ilya Sadykov
 * Hacky ArtifactStore. Just to override the default FilesToExtract with PostgresFilesToExtract
 */
public class PostgresArtifactStore extends ArtifactStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresArtifactStore.class);
    private final DownloadConfig downloadConfig;
    private final Directory tempDirFactory;
    private final TempNaming executableNaming;
    private final Downloader downloader;

    PostgresArtifactStore(DownloadConfig downloadConfig, Directory tempDirFactory, TempNaming executableNaming, Downloader downloader) {
        this.downloadConfig = downloadConfig;
        this.tempDirFactory = tempDirFactory;
        this.executableNaming = executableNaming;
        this.downloader = downloader;
    }

    @Override
    public void removeFileSet(Distribution distribution, ExtractedFileSet all) {
        for (File file : all.libraryFiles()) {
            if (file.exists() && !deleteQuietly(file))
                LOGGER.trace("Could not delete library NOW: {}", file);
        }
        File exe = all.executable();
        if (exe.exists() && !deleteQuietly(exe)) {
            LOGGER.trace("Could not delete executable NOW: {}", exe);
        }

        if (all.baseDirIsGenerated() && !deleteQuietly(all.baseDir())) {
            LOGGER.trace("Could not delete generatedBaseDir: {}", all.baseDir());
        }
    }

    @Override
    public DownloadConfig downloadConfig() {
        return downloadConfig;
    }

    @Override
    public Directory tempDirFactory() {
        return tempDirFactory;
    }

    @Override
    TempNaming executableNaming() {
        return executableNaming;
    }

    @Override
    Downloader downloader() {
        return downloader;
    }

    private boolean checkDistribution(Distribution distribution) throws IOException {
        return LocalArtifactStore.checkArtifact(downloadConfig(), distribution) || LocalArtifactStore
            .store(downloadConfig(), distribution, downloader().download(downloadConfig(), distribution));
    }

    @Override
    public Optional<ExtractedFileSet> extractFileSet(Distribution distribution) throws IOException {
        if (checkDistribution(distribution)) {
            PackageResolver packageResolver = downloadConfig.getPackageResolver();
            File artifact = getArtifact(downloadConfig, distribution);
            final ArchiveType archiveType = packageResolver.packageFor(distribution).archiveType();
            Extractor extractor = Extractors.getExtractor(archiveType);
            try {
                final FileSet fileSet = packageResolver.packageFor(distribution).fileSet();
                return Optional.of(extractor.extract(downloadConfig, artifact,
                        new PostgresFilesToExtract(tempDirFactory, executableNaming, fileSet, distribution)));
            } catch (Exception e) {
                LOGGER.error("Failed to extract file set:", e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private File getArtifact(DownloadConfig runtime, Distribution distribution) {
        File dir = createOrGetBaseDir(runtime);
        File artifactFile = new File(dir, runtime.getPackageResolver().packageFor(distribution).archivePath());
        if ((artifactFile.exists()) && (artifactFile.isFile()))
            return artifactFile;
        return null;
    }

    private File createOrGetBaseDir(DownloadConfig runtime) {
        File dir = runtime.getArtifactStorePath().asFile();
        createOrCheckDir(dir);
        return dir;
    }

    private void createOrCheckDir(File dir) {
        if (!dir.exists()) {
            LOGGER.debug("Required directory {}", dir);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalArgumentException("Could NOT create Directory " + dir);
        }
        if (!dir.isDirectory())
            throw new IllegalArgumentException("" + dir + " is not a Directory");
    }
}
