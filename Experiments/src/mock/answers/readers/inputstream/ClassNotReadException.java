package mock.answers.readers.inputstream;

/**
 * @author Derrick Lockwood
 * @created 8/29/18.
 */
public class ClassNotReadException extends RuntimeException {
    public ClassNotReadException(Class<?> type) {
        super(type.toString());
    }

    public ClassNotReadException(String name, Class<?> type) {
        super(name + " : " + type.toString());
    }
}
