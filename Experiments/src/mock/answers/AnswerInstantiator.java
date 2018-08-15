package mock.answers;

import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Member;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class AnswerInstantiator implements ObjectInstantiator<Object> {

    private final Class<?> returnType;
    private final String name;
    private final Object[] params;
    private final Object proxy;
    private final Answer answer;

    public AnswerInstantiator(Answer answer, Class<?> returnType) {
        this(answer, null, returnType);
    }

    public AnswerInstantiator(Answer answer, Object proxy, Class<?> returnType) {
        this(answer, proxy, null, returnType);
    }

    public AnswerInstantiator(Answer answer, Object proxy, String name, Class<?> returnType) {
        this(answer, proxy, name, returnType, (Object[]) null);
    }

    public AnswerInstantiator(Answer answer, Object proxy, String name, Class<?> returnType, Object... params) {
        this.name = name;
        this.proxy = proxy;
        this.params = params;
        this.answer = answer;
        this.returnType = returnType;
    }


    @Override
    public Object newInstance() {
        try {
            return answer.handle(proxy, params, name, returnType);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable.getCause());
        }
    }
}
