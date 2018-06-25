import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

/**
 * @author Derrick Lockwood
 * @created 6/11/18.
 */
public class AFLAgent {
    public static void premain(String[] args, Instrumentation instrumentation) {
        System.out.println("AFL Agent Starting...");
        new AgentBuilder.Default().with(new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

            }

            @Override
            public void onTransformation(
                    TypeDescription typeDescription,
                    ClassLoader classLoader,
                    JavaModule module,
                    boolean loaded,
                    DynamicType dynamicType) {
                System.out.println("Transformed - " + typeDescription + ", type = " + dynamicType);
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                    boolean loaded) {

            }

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
                    Throwable throwable) {

            }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

            }
        }).type(ElementMatchers.any())
                .transform(((builder, typeDescription, classLoader, module) -> (DynamicType.Builder<?>) builder.method(
                        ElementMatchers.any()))).installOn(instrumentation);
    }
}
