package roart.graphindicator;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.model.GUISize;
import roart.model.ResultItem;

public abstract class GraphIndicator {

    protected static Logger log = LoggerFactory.getLogger(GraphIndicator.class);

    protected String title;
    protected MyConfig conf;

    public GraphIndicator(MyConfig conf, String string) {
        this.title = string;
        this.conf = conf;
    }

    abstract public boolean isEnabled();

    public String getResultItemTitle() {
        return title;
    }

    abstract public void getResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize);

}

