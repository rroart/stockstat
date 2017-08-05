package roart.pipeline;

import java.util.Map;

import roart.config.MyMyConfig;

public abstract class Pipeline {

    protected MyMyConfig conf;
    protected int category;

    public Pipeline(MyMyConfig conf, int category) {
        this.conf = conf;
        this.category = category;
    }

    public abstract Map<String, Object> getLocalResultMap();
    public abstract Map<Integer, Map<String, Object>> getResultMap();
}
