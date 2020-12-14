package ru.yandex.qatools.embed.postgresql.config;

import de.flapdoodle.embed.process.config.SupportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.embed.postgresql.Command;

import java.util.function.BiFunction;

public class PostgresSupportConfig implements SupportConfig {
    private static final Logger logger = LoggerFactory.getLogger (PostgresSupportConfig.class);
    private final Command command;

    public PostgresSupportConfig(Command command) {
        this.command = command;
    }

    @Override
    public String name() {
        return command.commandName();
    }

    @Override
    public String supportUrl() {
        return "https://github.com/yandex-qatools/postgresql-embedded/issues (archived)";
    }

    @Override
    public BiFunction<Class<?>, Exception, String> messageOnException() {
        return PostgresSupportConfig::messageOnException;
    }

    private static String messageOnException(final Class<?> context, final Exception exception) {
      logger.warn("Some issues on {} with exception", context, exception);
      return exception.getMessage();
    }
}
