package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.AbstractProcess;
import de.flapdoodle.embed.process.runtime.Executable;
import de.flapdoodle.embed.process.runtime.IStopable;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.IOException;

import static java.io.File.*;
import static org.apache.commons.lang3.SystemUtils.JAVA_HOME;

public abstract class AbstractPGProcess<E extends Executable<PostgresConfig, P>, P extends IStopable>
        extends AbstractProcess<PostgresConfig, E, P> {

    public AbstractPGProcess(Distribution distribution, PostgresConfig config, RuntimeConfig runtimeConfig, E executable) throws IOException {
        super(distribution, config, runtimeConfig, executable);
    }

    @Override
    protected void onBeforeProcessStart(ProcessBuilder processBuilder, PostgresConfig config, RuntimeConfig runtimeConfig) {
        /*
         * begin-of-bugix
         * There is a bug in de.flapdoodle.embed.process.runtime.ProcessControl.newProcessBuilder(java.util.List<java.lang.String>, java.util.Map<java.lang.String,java.lang.String>, boolean)
         * redirectErrorStream value not propagated to ProcessBuilder, thus make it here.
         */
        processBuilder.redirectErrorStream(true);
        // enf-of-bugfix
        if (config.credentials() != null) {
            processBuilder.environment().put("PGUSER", config.credentials().username());
            processBuilder.environment().put("PGPASSWORD", config.credentials().password());
        }
        processBuilder.environment().put("PATH",
                processBuilder.environment().get("PATH") + pathSeparatorChar
                        + JAVA_HOME + separator + "bin"
                        + pathSeparatorChar
                        + JAVA_HOME + separator + "jre" + separator + "bin"
        );
    }

    @Override
    protected void stopInternal() {

    }

    @Override
    protected void cleanupInternal() {

    }
}
