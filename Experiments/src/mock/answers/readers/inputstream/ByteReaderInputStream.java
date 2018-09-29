package mock.answers.readers.inputstream;

import mock.answers.Answer;
import mock.answers.BasicAnswer;
import mock.answers.FixedAnswer;
import mock.answers.ReturnTypeAnswer;
import mock.answers.readers.NaiveRegexGeneratorByteReader;
import mock.answers.readers.datatype.DataTypeMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Derrick Lockwood
 * @created 8/29/18.
 */
public abstract class ByteReaderInputStream extends DataInputStream implements ReturnTypeAnswer {

    protected final String name;
    protected ByteReaderInputStreamList byteReaderInputStreamList;
    protected DataTypeMap dataTypeMap;
    private Object object;
    protected BasicAnswer[] staticObjects;


    @SuppressWarnings("ConstantConditions")
    public ByteReaderInputStream(String name, BasicAnswer... staticObjects) {
        super(null);
        this.name = name;
        this.object = null;
        byteReaderInputStreamList = null;
        dataTypeMap = new DataTypeMap();
        this.staticObjects = staticObjects;
    }

    public void setByteReaderListLink(ByteReaderInputStreamList byteReaders) {
        this.byteReaderInputStreamList = byteReaders;
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            super.close();
        }
    }

    public void setInputStream(InputStream inputStream) throws IOException {
        close();
        this.in = inputStream;
    }

    public abstract  <T> T readObject(Class<T> type) throws IOException;

    private void setStaticObjects(BasicAnswer[] staticObjects) {
        this.staticObjects = staticObjects;
    }

    @Override
    public Object applyReturnType(Class<?> returnType, boolean forceReload) {
        if (object != null && !forceReload) {
            return object;
        }
        try {
            object = readObject(returnType);
        } catch (IOException e) {
            handleReadException(e);
        }
        object = postProcessing(returnType, object);
        return object;
    }

    @Override
    public Answer duplicate() {
        byteReaderInputStreamList.remove(this);
        ByteReaderInputStream byteReader = duplicateByteReader();
        byteReaderInputStreamList.add(byteReader);
        return byteReader;
    }

    public String toString() {
        return (name != null ? name : "") + " : " + Objects.toString(object);
    }

    protected abstract ByteReaderInputStream duplicateByteReader();

    protected abstract void handleReadException(IOException e);

    protected Object postProcessing(Class<?> returnType, Object object) {
        return object;
    }


    public static ByteReaderInputStream createByType(String name, String type, JSONObject jsonObject, BasicAnswer[] staticObjects) {
        if (type == null || jsonObject == null) {
            return new DefaultByteReaderInputStream(name);
        }
        switch (type) {
            case "range":
                return new RangeByteReaderInputStream(name, (long) jsonObject.get("from"), (long) jsonObject.get("to"), staticObjects);
            case "chunk":
                if (jsonObject.containsKey("size")) {
                    return new ChunkByteReaderInputStream(name, (int) (long) jsonObject.get("size"), staticObjects);
                } else {
                    return new ChunkByteReaderInputStream(name, staticObjects);
                }
            case "array":
                if (jsonObject.containsKey("size")) {
                    return new ArrayByteReaderInputStream(name, (int) (long) jsonObject.get("size"), staticObjects);
                } else {
                    return new ArrayByteReaderInputStream(name, staticObjects);
                }
            case "default":
            default:
                return new DefaultByteReaderInputStream(name, staticObjects);
        }
    }
}
