package harness;

import mock.TransformClassLoader;
import mock.TransformMockClass;
import mock.answers.Answer;
import mock.answers.BasicAnswer;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.DynamicType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Runner {

    private static Map<Integer, Integer> map;
    private static boolean isRunning;

    public static void main(String[] args) throws
            ClassNotFoundException,
            IOException,
            InterruptedException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException,
            NoSuchFieldException {

        //TODO: figure out how to get generic class then we don't have to do anything
        /*
        Possibly this in configuration file
        {
           "class":"java.util.ArrayList<Foo>"
           ...Definition for Foo class...
        }

        Size is determined by a bytereader of a single integer, long etc.
         */

        TransformClassLoader classLoader = new TransformClassLoader(
                new URL[]{new File("resources/testClasses").toURI().toURL()});

        TransformMockClass transformMockClass = new TransformMockClass("Foo");
        transformMockClass.applyMethod("Helldsafdso", "strTest");
        transformMockClass.applyMethod("Static Method Changed wowowow", "staticTest");
        BasicAnswer basicAnswer = new BasicAnswer() {
            int i = 0;
            @Override
            public Object apply(Object proxy, Object[] params, Class<?> returnType) {
                i++;
                return i;
            }

            @Override
            public Answer duplicate() {
                return null;
            }
        };
        transformMockClass.applyField("value", basicAnswer);


        classLoader.addTransformation(transformMockClass);


        //Create Builder and Transform incoming classes using the TransformClassLoader and then install using the Byte Buddy Agent
        classLoader.createAgentBuilder().installOn(ByteBuddyAgent.install());

        Method m = transformMockClass.loadMethod("staticTest");
        System.out.println(m.invoke(null));

        Object foo = transformMockClass.newInstance();
        m = transformMockClass.loadMethod("strTest");
        System.out.println(m.invoke(foo));

        Field f = transformMockClass.loadField("value");
        System.out.println(f.get(foo));
        transformMockClass.reloadInstanceVariables(foo, "value");
        System.out.println(f.get(foo));
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static String test() {
        try {
            System.out.println("In");
            try {
                System.out.println("In2");
            } finally {
                System.out.println("Here");
                return "Out2";
            }
        } finally {
            return "Out";
        }

    }
}
