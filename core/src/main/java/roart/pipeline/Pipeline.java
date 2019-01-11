package roart.pipeline;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.indicator.Indicator;

public abstract class Pipeline {

    protected static Logger log = LoggerFactory.getLogger(Pipeline.class);

    protected MyMyConfig conf;
    protected int category;

    public Pipeline(MyMyConfig conf, int category) {
        this.conf = conf;
        this.category = category;
    }

    public abstract Map<String, Object> getLocalResultMap();
    public abstract Map<Integer, Map<String, Object>> getResultMap();
    public abstract String pipelineName();
}
