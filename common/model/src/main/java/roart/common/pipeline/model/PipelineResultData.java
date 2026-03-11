package roart.common.pipeline.model;

import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialPipeline;

public abstract class PipelineResultData {

    private SerialPipeline data = new SerialPipeline();
    
    public abstract SerialPipeline putData();

    public SerialPipeline getData() {
        return data;
    }
}
