package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.IOException;

/**
 * initdb executor
 * (helper to initialize the DB)
 */
class InitDbExecutable extends AbstractPGExecutable<PostgresConfig, InitDbProcess> {

    public InitDbExecutable(Distribution distribution, PostgresConfig config, RuntimeConfig runtimeConfig, ExtractedFileSet exe) {
        super(distribution, config, runtimeConfig, exe);
    }

    @Override
    protected InitDbProcess start(Distribution distribution, PostgresConfig config, RuntimeConfig runtime)
            throws IOException {
        return new InitDbProcess<>(distribution, config, runtime, this);
    }


    @Override
    public synchronized void stop() {
        // We don't want to cleanup after this particular single invocation
    }
}