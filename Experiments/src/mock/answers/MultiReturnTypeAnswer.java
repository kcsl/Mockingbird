package mock.answers;

import method.AttributeClass;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 6/26/18.
 */
public class MultiReturnTypeAnswer implements ReturnTypeAnswer {

    private final AttributeClass attributeClass;
    private final Object answers;
    private final ReturnTypeAnswer originalAnswer;

    public MultiReturnTypeAnswer(AttributeClass attributeClass, ReturnTypeAnswer originalAnswer) {
        this.attributeClass = attributeClass;
        this.originalAnswer = originalAnswer;
        answers = getMultiDimensionalArray(attributeClass.getAttribute(AttributeClass.DIMENSIONS));
    }

    private static Object constructPrimitiveArray(Class<?> returnType, boolean forceReload, Object answers,
            int[] dimensions, int dimIndex) {
        if (dimIndex >= dimensions.length - 1) {
            Object arr = Array.newInstance(returnType, dimensions[dimIndex]);
            for (int i = 0; i < dimensions[dimIndex]; i++) {
                Array.set(arr, i, ((ReturnTypeAnswer) Array.get(answers, i)).applyReturnType(returnType, forceReload));
            }
            return arr;
        } else {
            Object arr = Array.newInstance(Object.class, dimensions[dimIndex]);
            for (int i = 0; i < dimensions[dimIndex]; i++) {
                Array.set(arr, i,
                        constructPrimitiveArray(returnType, forceReload, Array.get(answers, i), dimensions,
                                dimIndex + 1));
            }
            return arr;
        }
    }

    @SuppressWarnings("unchecked")
    private static List constructPrimitiveList(Class<?> returnType, boolean forceReload, Object answers,
            int[] dimensions, int dimIndex) {
        if (dimIndex >= dimensions.length - 1) {
            List list = new ArrayList();
            for (int i = 0; i < dimensions[dimIndex]; i++) {
                list.add(i, ((ReturnTypeAnswer) Array.get(answers, i)).applyReturnType(returnType, forceReload));
            }
            return list;
        } else {
            List list = new ArrayList();
            for (int i = 0; i < dimensions[dimIndex]; i++) {
                list.add(i, constructPrimitiveList(returnType, forceReload, Array.get(answers, i), dimensions,
                        dimIndex + 1));
            }
            return list;
        }
    }

    @Override
    public Object applyReturnType(Class<?> returnType, boolean forceReload) {
        if (attributeClass.getAttribute(AttributeClass.IS_ARRAY, false)) {
            return constructPrimitiveArray(returnType, forceReload, answers,
                    attributeClass.getAttribute(AttributeClass.DIMENSIONS), 0);
        } else if (attributeClass.getAttribute(AttributeClass.IS_LIST, false)) {
            return constructPrimitiveList(returnType, forceReload, answers,
                    attributeClass.getAttribute(AttributeClass.DIMENSIONS), 0);
        }
        throw new RuntimeException("Multiple type not supported " + attributeClass);
    }

    private Object getMultiDimensionalArray(int[] dimensions) {
        return getMultiDimensionalArray(dimensions, 0);
    }

    private Object getMultiDimensionalArray(int[] dimensions, int index) {
        if (index >= dimensions.length) {
            return originalAnswer.duplicate();
        } else {
            Object arr = Array.newInstance(Object.class, dimensions[index]);
            for (int i = 0; i < dimensions[index]; i++) {
                Array.set(arr, i, getMultiDimensionalArray(dimensions, index + 1));
            }
            return arr;
        }
    }

    @Override
    public Answer duplicate() {
        return new MultiReturnTypeAnswer(attributeClass, originalAnswer);
    }
}
