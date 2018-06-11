package harness;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class TestClass {

    private Foo instanceObject;
    private Foo instanceObjectTwo;

    public TestClass() {
        instanceObject = new Foo();
    }

    private int methodToMock(Foo foo1, Foo foo2) {
        return foo1.test(foo2);
    }

    public int methodToMock1(int one, int two) {
        int three = one + two;
        three += one * two;
        return three;
    }

    public String methodToMock2() {
        return instanceObject.strTest();
    }
}
