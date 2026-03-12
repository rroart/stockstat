package roart.pipeline;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialPipeline;
import roart.iclij.config.IclijConfig;

public abstract class Pipeline {

    protected static Logger log = LoggerFactory.getLogger(Pipeline.class);

    protected IclijConfig conf;
    protected int category;

    private SerialPipeline data = new SerialPipeline();
        
    public Pipeline(IclijConfig conf, int category) {
        this.conf = conf;
        this.category = category;
    }

    public abstract SerialPipeline putData();
    public abstract String pipelineName();
    
    public SerialPipeline getData() {
        return data;
    }
}
