package mock.answers.readers.datatype;

import mock.answers.BasicAnswer;

import java.io.DataInput;
import java.io.IOException;
import java.util.function.Function;

/**
 * @author Derrick Lockwood
 * @created 8/30/18.
 */
public interface DataTypeFunction<R> {

    R apply(DataInput dataInput, DataTypeMap map, BasicAnswer... answers) throws IOException;
}
