package mock.answers;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class FixedAnswer implements BasicAnswer {

    private final Object value;

    public FixedAnswer(Object value) {
        this.value = value;
    }

    @Override
    public Object apply(Object proxy, Object[] params, Class<?> returnType) {
        return value;
    }

    @Override
    public Answer duplicate() {
        //TODO: Duplicate value? object.clone ?
        return new FixedAnswer(value);
    }

    @Override
    public String toString() {
        return "Fixed Answer: " + (value != null ? value : "null");
    }
}
