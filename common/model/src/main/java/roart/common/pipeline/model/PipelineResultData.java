package roart.common.pipeline.model;

import roart.common.pipeline.data.PipelineData;

public abstract class PipelineResultData {

    private PipelineData data = new PipelineData();
    
    public abstract PipelineData putData();

    public PipelineData getData() {
        return data;
    }
}
