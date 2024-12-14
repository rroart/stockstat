package roart.component.model;

import java.util.List;

import roart.common.pipeline.data.SerialList;
import roart.result.model.ResultMeta;

public abstract class ComponentMLData extends ComponentData {

    private SerialList resultMeta;

    private List<List> resultMetaArray;

    public ComponentMLData(ComponentData componentparam) {
        super(componentparam);
    }

    public SerialList getResultMeta() {
        return resultMeta;
    }

    public void setResultMeta(SerialList resultMeta) {
        this.resultMeta = resultMeta;
    }

    public List<List> getResultMetaArray() {
        return resultMetaArray;
    }

    public void setResultMetaArray(List<List> resultMetaArray) {
        this.resultMetaArray = resultMetaArray;
    }
    
}
