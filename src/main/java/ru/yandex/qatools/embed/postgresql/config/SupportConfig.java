package ru.yandex.qatools.embed.postgresql.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.embed.postgresql.Command;

import java.util.function.BiFunction;

public class SupportConfig implements de.flapdoodle.embed.process.config.SupportConfig {
    private static final Logger logger = LoggerFactory.getLogger (SupportConfig.class);
    private final Command command;

    public SupportConfig(Command command) {
        this.command = command;
    }

    @Override
    public String name() {
        return command.commandName();
    }

    @Override
    public String supportUrl() {
        return "https://github.com/yandex-qatools/postgresql-embedded/issues\n";
    }

    @Override
    public BiFunction<Class<?>, Exception, String> messageOnException() {
        return SupportConfig::messageOnException;
    }

    @Override
    public long maxStopTimeoutMillis() {
        return 100;
    }

    private static String messageOnException(final Class<?> context, final Exception exception) {
      logger.warn("Some issues on {} with exception", context, exception);
      return null;
    }
}