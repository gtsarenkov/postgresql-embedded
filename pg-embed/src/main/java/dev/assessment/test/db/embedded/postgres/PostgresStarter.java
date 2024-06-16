package dev.assessment.test.db.embedded.postgres;

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.embed.process.io.directories.TempDir;
import de.flapdoodle.embed.process.io.progress.ProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.store.ContentHashExtractedFileSetStore;
import de.flapdoodle.embed.process.store.DownloadCache;
import de.flapdoodle.embed.process.store.ExtractedFileSetStore;
import de.flapdoodle.embed.process.store.LocalDownloadCache;
import de.flapdoodle.embed.process.transitions.*;
import de.flapdoodle.embed.process.types.*;
import de.flapdoodle.os.CommonOS;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A class used to start and stop a local instance of PostgreSQL database.
 * This class provides methods to start and stop the database server, as well as
 * to check the status of the server.
 * <p>
 * To start the PostgreSQL server, simply call the {@link #start()} method.
 * To stop the PostgreSQL server, call the {@link #stop()} method.
 * To check the status of the server, use the {@link #isRunning()} method.
 * <p>
 * Note that this class assumes that the PostgreSQL database binaries are
 * already installed and available in the system's PATH environment variable.
 * Additionally, this class requires administrative privileges to start and stop
 * the database server.
 */
public class PostgresStarter {

  public static Transitions getInstance () {
    return Transitions.from (InitTempDirectory.withPlatformTempRandomSubDir (), Derive.given (TempDir.class)
                                                                                      .state (ProcessWorkingDir.class)
                                                                                      .with (Directories.deleteOnTearDown (TempDir.createDirectoryWith ("workDir"), ProcessWorkingDir::of)),
                             Derive.given (TempDir.class)
                                   .state (DownloadCache.class)
                                   .deriveBy (tempDir -> new LocalDownloadCache (tempDir.value ()
                                                                                        .resolve ("archives")))
                                   .withTransitionLabel ("setup DownloadCache"), Derive.given (de.flapdoodle.embed.process.io.directories.TempDir.class)
                                                                                       .state (ExtractedFileSetStore.class)
                                                                                       .deriveBy (tempDir -> new ContentHashExtractedFileSetStore (tempDir.value ()
                                                                                                                                                          .resolve ("fileSets")))
                                                                                       .withTransitionLabel ("setup ExtractedFileSetStore"),

                             Start.to (Name.class)
                                  .initializedWith (Name.of ("pg_ctl"))
                                  .withTransitionLabel ("create Name"),

                             Start.to (SupportConfig.class)
                                  .initializedWith (SupportConfig.generic ())
                                  .withTransitionLabel ("create default"), Start.to (ProcessConfig.class)
                                                                                .initializedWith (ProcessConfig.defaults ())
                                                                                .withTransitionLabel ("create default"), Start.to (ProcessEnv.class)
                                                                                                                              .initializedWith (ProcessEnv.of (postgreSqlEnv ()))
                                                                                                                              .withTransitionLabel ("create empty env"),

                             Start.to (Version.class)
                                  .initializedWith (Version.of ("16.3-2"))
                                  .withTransitionLabel ("set version"), Derive.given (Name.class)
                                                                              .state (ProcessOutput.class)
                                                                              .deriveBy (name -> ProcessOutput.namedConsole (name.value ()))
                                                                              .withTransitionLabel ("create named console"),

                             Start.to (ProgressListener.class)
                                  .providedBy (StandardConsoleProgressListener::new)
                                  .withTransitionLabel ("progressListener"),

                             Start.to (ProcessArguments.class)
                                  .initializedWith (ProcessArguments.of (Arrays.asList ("--help")))
                                  .withTransitionLabel ("create arguments"),

                             Derive.given (Version.class)
                                   .state (Distribution.class)
                                   .deriveBy (version -> Distribution.detectFor (CommonOS.list (), version))
                                   .withTransitionLabel ("version + platform"),

                             PackageOfDistribution.with (dist -> Package.builder ()
                                                                        .archiveType (ArchiveType.ZIP)
                                                                        .fileSet (FileSet.builder ()
                                                                                         .addEntry (FileType.Executable, "pg_ctl")
                                                                                         .build ())
                                                                        //.url(serverUrl + "phantomjs-" + dist.version().asInDownloadPath() + "-linux-x86_64.tar.bz2")
                                                                        .url ("%spostgresql-%s-windows-x64-binaries.zip".formatted (16.3 - 2))
                                                                        .build ()),

                             DownloadPackage.withDefaults (),

                             ExtractPackage.withDefaults ()
                                           .withExtractedFileSetStore (StateID.of (ExtractedFileSetStore.class)),

                             Executer.withDefaults ());
  }

  private static Map<String, ? extends String> postgreSqlEnv () {
    return new HashMap<> ();
  }
}
