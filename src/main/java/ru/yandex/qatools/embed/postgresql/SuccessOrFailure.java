package ru.yandex.qatools.embed.postgresql;

import org.slf4j.Logger;

import java.io.StringWriter;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class SuccessOrFailure implements Consumer<String> {
    private static final Logger LOGGER = getLogger (SuccessOrFailure.class);
    private final String successOutput;
    private final Set<String> failureOutput;
    private final AtomicBoolean isSuccess = new AtomicBoolean (false);
    private final AtomicBoolean isFailure = new AtomicBoolean (false);
    private final Command cmd;
    private String failureFound;
    private final AtomicLong lineCounter = new AtomicLong (0);
    private final StringWriter outputWriter = new StringWriter ();

    public SuccessOrFailure (Command cmd, String successOutput, Set<String> failureOutput) {
        this.successOutput = Objects.requireNonNull (successOutput, "successOutput cannot be null").toLowerCase ();
        this.failureOutput = Objects.requireNonNull (failureOutput, "failureOutput cannot be null").stream ().map (String::toLowerCase).collect (Collectors.toSet ());
        this.cmd = Objects.requireNonNull (cmd, "cmd cannot be null");
    }

    @Override
    public void accept (String s) {
        outputWriter.append (s).append ("\n");
        String filter = s.toLowerCase ();
        long lineNo = lineCounter.incrementAndGet ();
        if (!successOutput.isEmpty () && filter.contains (successOutput)) {
            if (!this.isSuccess.compareAndSet (false, true)) {
                LOGGER.info ("[{}]: line {}/success: {}/failure: {} - caught success output: {}", cmd, lineNo, this.isSuccess.get (), this.isFailure.get (), s);
            }
            else {
                synchronized (this) {
                    this.notifyAll ();
                }
            }
        }
        else if (failureOutput.stream ().anyMatch (filter::contains)) {
            if (!this.isFailure.compareAndSet (false, true)) {
                LOGGER.warn ("[{}]: line {}/success: {}/failure: {} - caught failure output: {}", cmd, lineNo, this.isSuccess.get (), this.isFailure.get (), s);
                failureFound = s;
            }
            else {
                synchronized (this) {
                    this.notifyAll ();
                }
            }
        }
        else {
            LOGGER.debug ("[{}]: line {}/success: {}/failure: {} - caught output: {}", cmd, lineNo, this.isSuccess.get (), this.isFailure.get (), filter);
        }
    }

    public boolean isInitWithSuccess () {
        return this.isSuccess.get ();
    }

    public boolean isInitWithFailure () {
        return this.isFailure.get ();
    }

    public String getFailureFound () {
        return this.failureFound;
    }

    public String getOutput () {
        return outputWriter.toString ();
    }

    public synchronized void waitForResult (long defaultCmdTimeout) {
        long sarted = System.currentTimeMillis ();
        try {
            this.wait (defaultCmdTimeout);
        }
        catch (InterruptedException e) {
            LOGGER.warn ("Command {} interrupted", cmd);
            if (this.isFailure.compareAndSet (false, true)) {
                // When interrupted, just
                this.notifyAll ();
            }
        }
        finally {
            long finished = System.currentTimeMillis ();
            if (!this.isSuccess.get () && !this.isFailure.get ()) {
                LOGGER.warn ("[{}] command timeout after {}ms", cmd, finished - sarted);
            }
            else if (this.isSuccess.get () || this.isFailure.get ()) {
                LOGGER.warn ("[{}] command detected after {}ms", cmd, finished - sarted);
            }
        }
    }

    public synchronized void close () {
        this.notifyAll ();
    }
}
