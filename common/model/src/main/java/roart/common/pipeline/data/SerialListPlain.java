package roart.common.pipeline.data;

import java.util.ArrayList;
import java.util.List;

public class SerialListPlain extends SerialObject {
    private List list = new ArrayList<>();

    public SerialListPlain() {
        super();
    }

    public SerialListPlain(List list) {
        super();
        this.list = list;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public void add(Object incdec) {
        list.add(incdec);
    }

    public Object get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

}
