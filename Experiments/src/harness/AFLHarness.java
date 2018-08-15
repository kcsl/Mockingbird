package harness;

import instrumentor.MockClassTransformer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * @author Derrick Lockwood
 * @created 6/8/18.
 */
public class AFLHarness {
    public static void premain(final String agentArgs,
            final Instrumentation inst) throws Exception {

        ElementMatcher<? super TypeDescription> elementMatcher = ElementMatchers.named("");

        new AgentBuilder.Default()
                .type(elementMatcher)
                .transform(new MockClassTransformer())
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .installOn(inst);
    }

}
