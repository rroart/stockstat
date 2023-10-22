package roart.pipeline;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.pipeline.data.PipelineData;
import roart.iclij.config.IclijConfig;

public abstract class Pipeline {

    protected static Logger log = LoggerFactory.getLogger(Pipeline.class);

    protected IclijConfig conf;
    protected int category;

    private PipelineData data;
        
    public Pipeline(IclijConfig conf, int category) {
        this.conf = conf;
        this.category = category;
    }

    public abstract PipelineData putData();
    public abstract String pipelineName();
    
    public PipelineData getData() {
        return data;
    }
}
