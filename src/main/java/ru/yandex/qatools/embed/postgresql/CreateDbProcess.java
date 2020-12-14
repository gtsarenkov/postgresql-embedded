package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * createdb process
 * (helper to initialize the DB)
 */
class CreateDbProcess extends AbstractPGProcess<CreateDbExecutable, CreateDbProcess> {

    public CreateDbProcess(Distribution distribution, PostgresConfig config, RuntimeConfig runtimeConfig, CreateDbExecutable executable) throws IOException {
        super(distribution, config, runtimeConfig, executable);
    }

    @Override
    protected List<String> getCommandLine(Distribution distribution, PostgresConfig config, ExtractedFileSet exe)
            throws IOException {
        List<String> ret = new ArrayList<>();
        ret.add(exe.executable().getAbsolutePath());
        ret.addAll(Arrays.asList(
                "-h", config.net().host(),
                "-p", String.valueOf(config.net().port())
        ));
        ret.add(config.storage().dbName());

        return ret;
    }
}
