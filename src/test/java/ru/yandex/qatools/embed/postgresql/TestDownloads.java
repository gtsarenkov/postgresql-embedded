package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.store.IArtifactStore;
import de.flapdoodle.embed.process.store.ImmutableArtifactStore;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import de.flapdoodle.os.*;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import ru.yandex.qatools.embed.postgresql.distribution.PostgreSQLVersion;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestDownloads {

    /** Version 11 binary downloads are available for OS X and Windows 64 bit only */
    private boolean supported(Distribution distribution) {
        if (distribution.version().asInDownloadPath().startsWith("9.")
            || distribution.version().asInDownloadPath().startsWith("10.")) {
            return true;
        }
        switch (distribution.platform().operatingSystem ()) {
        case OS_X:
            return true;
        case Windows:
            return distribution.platform ().architecture ().bitSize () == BitSize.B64;
        default:
            return false;
        }
    }

    @Test
    public void testDownloads() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IArtifactStore artifactStore = new PostgresArtifactStoreBuilder().defaults(Command.Postgres).build(ImmutableArtifactStore.Builder::build);

        for (OS p : asList(OS.OS_X, OS.Linux, OS.Windows)) {
            for (Architecture architecture : asList(CommonArchitecture.X86_32, CommonArchitecture.X86_64)) {
                for (de.flapdoodle.embed.process.distribution.Version version : PostgreSQLVersion.Main.values()) {
                    Distribution distribution = Distribution.of(version, ImmutablePlatform.builder ().operatingSystem (p).architecture (
                        architecture).build());
                    if (! supported(distribution)) {
                        continue;
                    }
                    Class<?> classCandidate = artifactStore.getClass ();
                    Method method;
                    do {
                        try {
                            if (Objects.nonNull(classCandidate)) {
                                method = classCandidate.getDeclaredMethod("checkDistribution", Distribution.class);
                            }
                            else {
                                method = null;
                            }
                        }
                        catch (NoSuchMethodException ignored) {
                            method = null;
                            classCandidate = classCandidate.getSuperclass();
                        }
                    } while (Objects.isNull(method) && Objects.nonNull(classCandidate));
                    // If method checkDistribution not found we should fail test. Let it be NullPointerException for now.
                    method.setAccessible(true);
                    MatcherAssert.assertThat("Distribution: " + distribution + " should be accessible", method.invoke(artifactStore, distribution), is(true));
                }
            }
        }
    }
}
