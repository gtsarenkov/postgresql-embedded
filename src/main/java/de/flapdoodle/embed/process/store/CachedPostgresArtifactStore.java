package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet.Builder;
import de.flapdoodle.embed.process.io.directories.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

import static de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet.builder;
import static java.nio.file.Files.exists;
import static java.nio.file.Paths.get;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

public class CachedPostgresArtifactStore extends PostgresArtifactStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedPostgresArtifactStore.class);
    private final DownloadConfig downloadConfig;
    private final Directory eDir;

    public CachedPostgresArtifactStore(DownloadConfig downloadConfig, Directory eDir,
                                       ITempNaming executableNaming, IDownloader downloader) {
        super(downloadConfig, eDir, executableNaming, downloader);
        this.downloadConfig = downloadConfig;
        this.eDir = eDir;
    }

    @Override
    public void removeFileSet(Distribution distribution, ExtractedFileSet all) {
        // do nothing
    }

    @Override
    public ExtractedFileSet extractFileSet(Distribution distribution) {
        try {
            final File dir = this.eDir.asFile();
            final FileSet filesSet = downloadConfig.getPackageResolver().packageFor (distribution).fileSet ();
            final Path path = get(dir.getPath(),
                    "pgsql" + "-" + distribution.version ().asInDownloadPath(), "pgsql");
            if (exists(path)) {
                final Builder extracted = builder(dir).baseDirIsGenerated(false);
                iterateFiles(path.toFile(), TRUE, TRUE).forEachRemaining(file -> {
                    if (filesSet.entries().stream()
                            .anyMatch(entry -> entry.matchingPattern().matcher(file.getPath()).matches())) {
                        extracted.executable (file);
                    }
                    else {
                      extracted.addLibraryFiles (file);
                    }
                });
                return extracted.build();
            } else {
                return super.extractFileSet(distribution);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to extract file set", e);
            return new EmptyFileSet();
        }
    }
}
