package dev.assessment.test.db.embedded.postgres;

/**
 * A class used to start and stop a local instance of PostgreSQL database.
 * This class provides methods to start and stop the database server, as well as
 * to check the status of the server.
 *
 * To start the PostgreSQL server, simply call the {@link #start()} method.
 * To stop the PostgreSQL server, call the {@link #stop()} method.
 * To check the status of the server, use the {@link #isRunning()} method.
 *
 * Note that this class assumes that the PostgreSQL database binaries are
 * already installed and available in the system's PATH environment variable.
 * Additionally, this class requires administrative privileges to start and stop
 * the database server.
 */
public class PostgresStarter {
}
