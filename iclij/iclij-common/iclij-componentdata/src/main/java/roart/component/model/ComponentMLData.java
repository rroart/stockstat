package roart.component.model;

import java.util.List;

import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialMeta;
import roart.common.pipeline.data.SerialResultMeta;
import roart.result.model.ResultMeta;

public abstract class ComponentMLData extends ComponentData {

    private List<SerialResultMeta> resultMeta;

    public ComponentMLData(ComponentData componentparam) {
        super(componentparam);
    }

    public List<SerialResultMeta> getResultMeta() {
        return resultMeta;
    }

    public void setResultMeta(List<SerialResultMeta> resultMeta) {
        this.resultMeta = resultMeta;
    }

}
