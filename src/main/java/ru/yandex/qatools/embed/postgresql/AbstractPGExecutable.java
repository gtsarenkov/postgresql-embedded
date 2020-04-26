package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Executable;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;

public abstract class AbstractPGExecutable<C extends AbstractPostgresConfig, P extends AbstractPGProcess>
        extends Executable<C, P> {

    public AbstractPGExecutable(Distribution distribution, C config, RuntimeConfig runtimeConfig, ExtractedFileSet executable) {
        super(distribution, config, runtimeConfig, executable);
    }
}