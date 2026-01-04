package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDbProcess.class);

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
        if (LOGGER.isDebugEnabled()) {
            ret.add("--echo");
        }
        ret.add(config.storage().dbName());

        return ret;
    }
}
