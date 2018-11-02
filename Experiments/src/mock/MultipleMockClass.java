package mock;

import method.AttributeClass;
import mock.answers.Answer;
import mock.answers.MultipleMockAnswer;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static mock.answers.MultipleMockAnswer.getMultiAnswer;

/**
 * @author Derrick Lockwood
 * @created 6/25/18.
 */
public class MultipleMockClass extends SubMockClass {

    private final AttributeClass attributeClass;

    public MultipleMockClass(TargetedMockBuilderDEL targetedMockBuilderDEL, DynamicType.Builder<?> builder,
            AttributeClass attributeClass) {
        super(targetedMockBuilderDEL, attributeClass.getMockClass(), builder);
        this.attributeClass = attributeClass;
        super.createField(MultipleMockAnswer.INDEX_FIELD, int.class, Modifier.PUBLIC);
    }

    public MockClass applyMethod(Answer answer, Method method) {
        int[] dimensions = attributeClass.getAttribute(AttributeClass.DIMENSIONS);
        return super.applyMethod(getMultiAnswer(dimensions, answer), method);
    }

    @Override
    public Object newInstance() {
        if (attributeClass.getAttribute(AttributeClass.IS_ARRAY, false)) {
            int[] dimensions = attributeClass.getAttribute(AttributeClass.DIMENSIONS);
            try {
                return getMultiDimensionalArray(dimensions);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            }
        } else if (attributeClass.getAttribute(AttributeClass.IS_LIST, false)) {
            int[] dimensions = attributeClass.getAttribute(AttributeClass.DIMENSIONS);
            try {
                return getMultiDimensionalList(dimensions);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            }
        }
        throw new RuntimeException("Multiple type not supported " + attributeClass.getAttribute(AttributeClass.TYPE));
    }

    private Object getMultiDimensionalList(int[] dimensions) throws NoSuchFieldException, IllegalAccessException {
        return getMultiDimensionalList(dimensions, 0);
    }

    @SuppressWarnings("unchecked")
    private Object getMultiDimensionalList(int[] dimensions, int index) throws
            NoSuchFieldException,
            IllegalAccessException {
        if (index >= dimensions.length - 1) {
            Field field = super.newInstance().getClass().getDeclaredField(MultipleMockAnswer.INDEX_FIELD);
            List list = new ArrayList();
            for (int i = 0; i < dimensions[index]; i++) {
                Object object = super.newInstance();
                field.set(object, i);
                list.add(i, object);
            }
            return list;
        } else {
            List list = new ArrayList();
            for (int i = 0; i < dimensions[index]; i++) {
                list.add(getMultiDimensionalList(dimensions, index + 1));
            }
            return list;
        }
    }

    private Object getMultiDimensionalArray(int[] dimensions) throws NoSuchFieldException, IllegalAccessException {
        return getMultiDimensionalArray(dimensions, 0);
    }

    private Object getMultiDimensionalArray(int[] dimensions, int index) throws
            NoSuchFieldException,
            IllegalAccessException {
        if (index >= dimensions.length - 1) {
            //Hack to get the new type to be created of this instance change maybe?
            Field field = super.newInstance().getClass().getDeclaredField(MultipleMockAnswer.INDEX_FIELD);
            Object arr = Array.newInstance(getNewType(), dimensions[index]);
            for (int i = 0; i < dimensions[index]; i++) {
                Object object = super.newInstance();
                field.set(object, i);
                Array.set(arr, i, object);
            }
            return arr;
        } else {
            Object arr = Array.newInstance(Object.class, dimensions[index]);
            for (int i = 0; i < dimensions[index]; i++) {
                Array.set(arr, i, getMultiDimensionalArray(dimensions, index + 1));
            }
            return arr;
        }
    }

}
