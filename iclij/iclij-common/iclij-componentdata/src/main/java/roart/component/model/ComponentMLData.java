package roart.component.model;

import java.util.List;

import roart.result.model.ResultMeta;

public abstract class ComponentMLData extends ComponentData {

    private List<ResultMeta> resultMeta;

    private List<List> resultMetaArray;

    public ComponentMLData(ComponentData componentparam) {
        super(componentparam);
    }

    public List<ResultMeta> getResultMeta() {
        return resultMeta;
    }

    public void setResultMeta(List<ResultMeta> resultMeta) {
        this.resultMeta = resultMeta;
    }

    public List<List> getResultMetaArray() {
        return resultMetaArray;
    }

    public void setResultMetaArray(List<List> resultMetaArray) {
        this.resultMetaArray = resultMetaArray;
    }
    
}
