package roart.common.pipeline.data;

import java.util.ArrayList;
import java.util.List;

public class SerialList extends SerialObject {
    private List<SerialObject> list = new ArrayList<>();

    public SerialList() {
        super();
    }

    public List<SerialObject> getList() {
        return list;
    }

    public void setList(List<SerialObject> list) {
        this.list = list;
    }

    public void add(SerialObject incdec) {
        list.add(incdec);
    }
}
