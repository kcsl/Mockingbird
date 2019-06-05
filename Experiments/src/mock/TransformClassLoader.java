package mock;

import instrumentor.AFLMethodVisitor;
import mock.answers.Answer;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import javax.sound.midi.Instrument;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 10/23/18.
 */
public class TransformClassLoader extends URLClassLoader implements AgentBuilder.Transformer {
    private final Map<String, TransformMockClass> transformMap;
    private final Objenesis objenesis;
    private ElementMatcher.Junction<? super TypeDescription> transformedTypes;
    private ElementMatcher.Junction<? super TypeDescription> transformedPackages;

    public TransformClassLoader(String... urlPaths) throws MalformedURLException {
        this(toURLs(urlPaths));
    }

    public TransformClassLoader(URL... urls) {
        super(urls);
        transformMap = new HashMap<>();
        objenesis = new ObjenesisStd();
        transformedTypes = ElementMatchers.none();
        transformedPackages = ElementMatchers.none();
    }

    public TransformClassLoader() {
        this((URL) null);
    }

    public AgentBuilder transformAgentBuilder(AgentBuilder agentBuilder) {
        return agentBuilder
                .type(transformedPackages.or(transformedTypes))
                .transform(this);
    }

    public AgentBuilder createAgentBuilder() {
        return transformAgentBuilder(new AgentBuilder.Default());
    }

    public ResettableClassFileTransformer loadAgent(Instrumentation instrumentation) {
        return createAgentBuilder().installOn(instrumentation);
    }

    public void addTransformation(TransformMockClass transformMockClass) {
        String canonicalName = transformMockClass.getCanonicalName();
        if (transformMap.containsKey(canonicalName)) {
            return;
        }
        transformedTypes = transformedTypes.or(ElementMatchers.named(canonicalName));
        transformMockClass.tiedClassLoader = this;
        transformMap.put(canonicalName, transformMockClass);
    }

    public TransformMockClass getTransformMockClass(String canonicalName) {
        return transformMap.get(canonicalName);
    }

    public TransformMockClass getTransformMockClassOrCreate(String canonicalName) {
        TransformMockClass t = getTransformMockClass(canonicalName);
        if (t == null) {
            t = new TransformMockClass(canonicalName);
            addTransformation(t);
        }
        return t;
    }

    public void addURL(URL url) {
        super.addURL(url);
    }

    public void addAppPackage(String packageName) {
        transformedPackages = transformedPackages.or(ElementMatchers.nameContains(packageName));
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        if (transformedTypes.matches(typeDescription)) {
            return transformMap.get(typeDescription.getCanonicalName()).transform(builder);
        } else {
            //No transform class but still needs AFL transformation
            return AFLMethodVisitor.applyAFLTransformation(builder, ElementMatchers.any());
        }
    }

    static Implementation getImplementation(Answer answer) {
        return MethodDelegation.withDefaultConfiguration().filter(ElementMatchers.named("handle").and(
                ElementMatchers.takesArguments(Object.class, Object[].class, Callable.class, Method.class)).or(
                ElementMatchers.takesArguments(Object.class, Object[].class, Method.class)).or(
                ElementMatchers.takesArguments(Object[].class))).to(answer, Answer.class);
    }

    private static URL[] foldURLs(URL app, URL... libs) {
        URL[] urls = new URL[libs.length + 1];
        urls[0] = app;
        for (int i = 1; i < urls.length; i++) {
            urls[i]= libs[i-1];
        }
        return urls;
    }

    <V> ObjectInstantiator<V> getInstantiator(Class<V> vClass) {
        if (vClass == null) {
            return null;
        }
        return objenesis.getInstantiatorOf(vClass);
    }

    private static URL[] toURLs(String... urlPaths) throws MalformedURLException {
        URL[] urls = new URL[urlPaths.length];
        for (int i = 0; i < urlPaths.length; i++) {
            urls[i] = new File(urlPaths[i]).toURI().toURL();
        }
        return urls;
    }

    public interface Transformer {
        DynamicType.Builder<?> transform(DynamicType.Builder<?> builder);
    }
}
