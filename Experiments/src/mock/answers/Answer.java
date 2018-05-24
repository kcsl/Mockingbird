package mock.answers;

/**
 * @author Derrick Lockwood
 * @created 5/22/18.
 */
public interface Answer<T> {
    T handle(InvocationData invocationData) throws Throwable;
}
