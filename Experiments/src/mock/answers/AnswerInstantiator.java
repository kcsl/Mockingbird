package mock.answers;

import org.objenesis.instantiator.ObjectInstantiator;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class AnswerInstantiator implements ObjectInstantiator<Object> {

    private final Class<?> returnType;
    private final Answer answer;

    public AnswerInstantiator(Answer answer, Class<?> returnType) {
        this.answer = answer;
        this.returnType = returnType;
    }

    @Override
    public Object newInstance() {
        try {
            return answer.handle(null, null, null, returnType);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable.getCause());
        }
    }
}
