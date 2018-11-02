package mock;

import instrumentor.AFLClassVisitor;
import instrumentor.AFLMethodVisitor;
import mock.answers.Answer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 10/23/18.
 */
public class TransformClassLoader extends URLClassLoader implements AgentBuilder.Transformer {
    private HashMap<String, Transformer> hashMap;
    private final Objenesis objenesis;

    public TransformClassLoader(URL[] urls) {
        super(urls);
        hashMap = new HashMap<>();
        objenesis = new ObjenesisStd();
    }

    public TransformClassLoader() {
        this(new URL[0]);
    }

    public AgentBuilder transformAgentBuilder(AgentBuilder agentBuilder) {
        return agentBuilder
                .type(ElementMatchers.any())
//                .transform(TransformClassLoader.createAFLTransformer(this))
                .transform(this);
    }

    public AgentBuilder createAgentBuilder() {
        return transformAgentBuilder(new AgentBuilder.Default());
    }

    public void addTransformation(String canonicalName, Transformer transformer) {
        hashMap.put(canonicalName, transformer);
    }

    public void addTransformation(TransformMockClass transformMockClass) {
        transformMockClass.tiedClassLoader = this;
        hashMap.put(transformMockClass.getCanonicalName(), transformMockClass);
    }

    public void addURL(URL url) {
        super.addURL(url);
    }


    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        if (this.equals(classLoader)) {
            Transformer t = hashMap.get(typeDescription.getCanonicalName());
            if (t != null) {
                builder = builder.method(ElementMatchers.any())
                        .intercept(MethodDelegation.withDefaultConfiguration().filter(AFLAnswer.MATCHER).to(new AFLAnswer()));
                return t.transform(builder);
            }
        }
        return builder;
    }

    static Implementation getImplementation(Answer answer) {
        return MethodDelegation.withDefaultConfiguration().filter(ElementMatchers.named("handle").and(
                ElementMatchers.takesArguments(Object.class, Object[].class, Callable.class, Method.class)).or(
                ElementMatchers.takesArguments(Object.class, Object[].class, Method.class)).or(
                ElementMatchers.takesArguments(Object[].class))).to(answer, Answer.class);
    }

    <V> V newInstance(Class<V> vClass) {
        if (vClass == null) {
            return null;
        }
        return objenesis.newInstance(vClass);
    }


    private static AgentBuilder.Transformer createAFLTransformer(TransformClassLoader cl) {
        return (builder, typeDescription, classLoader, module) -> {
            if (cl.equals(classLoader)) {
                        return builder.visit(AFLClassVisitor.create());
//                return builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.any(),
//                        (AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper) (instrumentedType, instrumentedMethod, methodVisitor, implementationContext, typePool, writerFlags, readerFlags) -> new AFLMethodVisitor(
//                                methodVisitor)));
            }
            return builder;
        };
    }

    public interface Transformer {
        DynamicType.Builder<?> transform(DynamicType.Builder<?> builder);

        default Transformer link(Transformer transformer) {
            Transformer self = this;
            return builder -> transformer.transform(self.transform(builder));
        }

        static Transformer empty() {
            return builder -> builder;
        }
    }
}
