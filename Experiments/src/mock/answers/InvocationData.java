package mock.answers;


import java.lang.reflect.Method;

/**
 * @author Derrick Lockwood
 * @created 5/22/18.
 */
public class InvocationData {
    private Object mockObject;
    private Method method;
    private Object[] params;

    public InvocationData(Object mockObject, Method method, Object[] params) {
        this.mockObject = mockObject;
        this.method = method;
        this.params = params;
    }


    public Object getMockObject() {
        return mockObject;
    }

    public Object getParam(int index) {
        return this.params[index];
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getParams() {
        return params;
    }
}
