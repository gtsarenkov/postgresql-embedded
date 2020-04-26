package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.directories.Directory;

/**
 * Paths builder
 */
public class PackagePaths implements PackageResolver {

    private final Command command;
    private final Directory tempDir;

    public PackagePaths(Command command, Directory tempDir) {
        this.command = command;
        this.tempDir = tempDir;
    }

    protected static String getVersionPart(Version version) {
        return version.asInDownloadPath();
    }

    public Directory getTempDir() {
        return tempDir;
    }

    @Override
    public DistributionPackage packageFor(final Distribution distribution) {
      return new DistributionPackage () {
        @Override
        public ArchiveType archiveType () {
          ArchiveType archiveType;
          switch (distribution.platform()) {
            case Linux:
              archiveType = ArchiveType.TGZ;
              break;
            case OS_X:
            case Windows:
              archiveType = ArchiveType.ZIP;
              break;
            default:
              throw new IllegalArgumentException("Unknown Platform "
                + distribution.platform());
          }
          return archiveType;
        }

        @Override
        public FileSet fileSet () {
          String cmdPattern;
          switch (distribution.platform()) {
            case Linux:
            case OS_X:
              cmdPattern = command.commandName();
              break;
            case Windows:
              cmdPattern = command.commandName() + ".exe";
              break;
            default:
              throw new IllegalArgumentException("Unknown Platform "
                + distribution.platform());
          }
          try {
            return FileSet.builder()
              .addEntry(FileType.Executable, tempDir.asFile().getPath(),
                "^.*pgsql\\\\bin\\\\" + cmdPattern + "$")
              .addEntry(FileType.Executable, tempDir.asFile().getPath(),
                "^.*pgsql/bin/" + cmdPattern + "$")
              .build();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public String archivePath () {
            String downloadVersion = getVersionPart(distribution.version());

            ArchiveType archiveType = archiveType();
            String sarchiveType;
            switch (archiveType) {
              case TGZ:
                sarchiveType = "tar.gz";
                break;
              case ZIP:
                sarchiveType = "zip";
                break;
              default:
                throw new IllegalArgumentException("Unknown ArchiveType "
                  + archiveType);
            }

            String splatform;
            switch (distribution.platform()) {
              case Linux:
                splatform = "linux";
                break;
              case Windows:
                splatform = "windows";
                break;
              case OS_X:
                splatform = "osx";
                break;
              default:
                throw new IllegalArgumentException("Unknown Platform "
                  + distribution.platform());
            }

            String bitsize = "";
            switch (distribution.bitsize()) {
              case B32:
                switch (distribution.platform()) {
                  case Windows:
                    switch (downloadVersion) {
                      case "9.5.21-1":
                        downloadVersion = "9.5.21-3";
                        break;
                    }
                    break;
                  case Linux:
                  case OS_X:
                    break;
                  default:
                    throw new IllegalArgumentException(
                      "32 bit supported only on Windows, MacOS, Linux, platform is "
                        + distribution.platform());
                }
                break;
              case B64:
                switch (distribution.platform()) {
                  case Linux:
                    bitsize = "-x64";
                    break;
                  case Windows:
                    bitsize = "-x64";
                    // win x64 has different download paths
                    // See https://github.com/yandex-qatools/postgresql-embedded/issues/109
                    switch (downloadVersion) {
                      case "10.1-1":
                        downloadVersion = "10.1-2";
                        break;
                      case "9.6.6-1":
                        downloadVersion = "9.6.6-2";
                        break;
                    }
                    break;
                  case OS_X:
                    break;
                  default:
                    throw new IllegalArgumentException(
                      "64 bit supported only on Linux and Windows, platform is "
                        + distribution.platform());
                }
                break;
              default:
                throw new IllegalArgumentException("Unknown BitSize " + distribution.bitsize());
            }

            return "postgresql-" + downloadVersion + "-" + splatform + bitsize + "-binaries" + "." + sarchiveType;
          }
        };
    }
}
