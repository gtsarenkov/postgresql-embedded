package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.extract.ExtractedFileSet;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * @author Ilya Sadykov
 */
public class EmptyFileSet extends ExtractedFileSet {
    @Override
    public File executable() {
        return null;
    }

    @Override
    public Set<File> libraryFiles() {
      return Collections.emptySet();
    }

    @Override
    public File baseDir() {
        return null;
    }

    @Override
    public boolean baseDirIsGenerated() {
        return false;
    }
}
