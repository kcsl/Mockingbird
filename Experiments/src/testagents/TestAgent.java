package testagents;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * @author Derrick Lockwood
 * @created 7/23/18.
 */
public class TestAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .type(ElementMatchers.nameContains("BigInteger"))
                .transform(((builder, type, classLoader, module) ->
                        builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(SpaceCMDMethodInterceptor.class))))
                .installOn(instrumentation);
    }
}
