package dev.assessment.test.db.embedded.postgres;

import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.graph.TransitionGraph;

/**
 * The CommandLineInterface class is responsible for starting a temporary embedded Postgres database.
 * It contains a main method that prints a starting message to the console.
 */
public class CommandLineInterface {
  public static void main (String[] args) {
    System.out.println ("Starting temporary embedded Postgres database");
    Transitions transitions = PostgresStarter.getInstance ();
    TransitionWalker init = transitions.walker();
  }
}
