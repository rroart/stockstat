package roart.pipeline;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;

public abstract class Pipeline {

    protected static Logger log = LoggerFactory.getLogger(Pipeline.class);

    protected IclijConfig conf;
    protected int category;

    public Pipeline(IclijConfig conf, int category) {
        this.conf = conf;
        this.category = category;
    }

    public abstract Map<String, Object> getLocalResultMap();
    public abstract String pipelineName();
}
