package dev.assessment.test.db.embedded.postgres;

import de.flapdoodle.embed.process.transitions.ProcessFactory;
import de.flapdoodle.os.CommonOS;
import de.flapdoodle.os.Distribution;
import de.flapdoodle.os.Platform;
import de.flapdoodle.os.Version;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.graph.TransitionGraph;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CommandLineInterfaceTest {
    @RegisterExtension
    public static Recording recording1 = Recorder.with("HowTo1.md", TabSize.spaces(2));

    @RegisterExtension
    public static Recording recording2 = Recorder.with("HowTo2.md", TabSize.spaces(2));

    @Test
    public void detectPlatform() {
        recording1.begin();
        Platform result = Platform.detect(CommonOS.list());
        recording1.end();
        assertThat(result).isNotNull();
        recording1.output("result.os", result.operatingSystem().name());
        recording1.output("result.architecture.cpuType", result.architecture().cpuType().name());
        recording1.output("result.architecture.bitSize", result.architecture().bitSize().name());
        recording1.output("result.distribution", result.distribution().map(Distribution::name).orElse("---"));
        recording1.output("result.version", result.version().map(Version::name).orElse("---"));
    }

    @Test
    public void initialVersion() {
        recording2.begin();
        Transitions transitions = PostgresStarter.getInstance();
        recording2.end();
//        TransitionWalker walker = transitions.walker();
        String dot = TransitionGraph.edgeGraphAsDot("process factory sample", transitions);
        recording2.output("sample.dot", dot);
    }

}