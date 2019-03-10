package mock.answers;

/**
 * @author Derrick Lockwood
 * @created 2019-02-08.
 */
public abstract class AnswerCreator implements ReturnTypeAnswer {

    public abstract Answer create();

    @Override
    public Object applyReturnType(Class<?> returnType, boolean forceReload) {
        if (returnType.isAssignableFrom(Answer.class)){
            return create();
        }
        throw new RuntimeException("Can't use an AnswerCreator to construct anything other than an Answer");
    }

    @Override
    public Answer duplicate() {
        //TODO: Unknown if this is a good thing or a bad thing haven't decided
        return new AnswerCreator() {
            @Override
            public Answer create() {
                return AnswerCreator.this.create();
            }
        };
    }
}
