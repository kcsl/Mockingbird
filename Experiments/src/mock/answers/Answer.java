package mock.answers;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public interface Answer {
    Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) throws Throwable;
}
