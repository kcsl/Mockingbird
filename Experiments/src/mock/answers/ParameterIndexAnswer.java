package mock.answers;

/**
 * @author Derrick Lockwood
 * @created 6/26/18.
 */
public class ParameterIndexAnswer implements ParameterAnswer {

    private final int index;

    public ParameterIndexAnswer(int index) {
        this.index = index;
    }

    @Override
    public Object getObject(Object[] parameters) {
        return parameters[index];
    }

    @Override
    public Answer duplicate() {
        return new ParameterIndexAnswer(index);
    }
}
