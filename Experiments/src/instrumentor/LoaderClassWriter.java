package instrumentor;

import org.objectweb.asm.ClassWriter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 7/9/18.
 */
public class LoaderClassWriter extends ClassWriter {

    private final List<ClassLoader> classLoaders;

    public LoaderClassWriter(int i) {
        super(i);
        classLoaders = new LinkedList<>();
        classLoaders.add(getClass().getClassLoader());
    }

    public void addClassLoader(ClassLoader classLoader) {
        classLoaders.add(classLoader);
    }

    public void addClassPath(String... classPaths) {
        addClassPath((URL[]) Arrays.stream(classPaths).map((str) -> {
            try {
                return new URL(str);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray());
    }

    public void addClassPath(URL... classPaths) {
        classLoaders.add(URLClassLoader.newInstance(classPaths));
    }

    public void removeClassLoader(ClassLoader classLoader) {
        classLoaders.remove(classLoader);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        Class<?> c = null;
        Class<?> d = null;

        for (ClassLoader classLoader : classLoaders) {
            try {
                c = Class.forName(type1.replace('/', '.'), false, classLoader);
                d = Class.forName(type2.replace('/', '.'), false, classLoader);
                break;
            } catch (Exception e) {
                continue;
            }
        }

        if (c == null || d == null) {
            throw new RuntimeException(
                    "Could not find common super class of: [type1=" + type1 + "], [type2=" + type2 + "]");
        }

        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }
}
