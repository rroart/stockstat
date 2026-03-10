package roart.common.pipeline.model;

import roart.common.pipeline.data.PipelineData;

public abstract class PipelineResultData {

    private PipelineData[] data = new PipelineData[0];
    
    public abstract PipelineData[] putData();

    public PipelineData[] getData() {
        return data;
    }
}
