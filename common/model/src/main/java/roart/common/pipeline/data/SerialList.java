package roart.common.pipeline.data;

import java.util.ArrayList;
import java.util.List;

import roart.result.model.ResultMeta;

public class SerialList<T extends SerialObject> extends SerialObject {
    private List<T> list = new ArrayList<>();

    public SerialList() {
        super();
    }

    public SerialList(List<T> list) {
        super();
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void add(T object) {
        list.add(object);
    }

    public SerialObject get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }
}
