package roart.common.pipeline.data;

import java.util.ArrayList;
import java.util.List;

public class SerialListPlain<T> extends SerialObject {
    private List<T> list = new ArrayList<>();

    public SerialListPlain() {
        super();
    }

    public SerialListPlain(List<T> list) {
        super();
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void add(T incdec) {
        list.add(incdec);
    }

    public T get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

}
