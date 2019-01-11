package roart.graphindicator;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;

public abstract class GraphIndicator {

    protected static Logger log = LoggerFactory.getLogger(GraphIndicator.class);

    protected String title;
    protected MyMyConfig conf;

    public GraphIndicator(MyMyConfig conf, String string) {
        this.title = string;
        this.conf = conf;
    }

    public abstract boolean isEnabled();

    public String getResultItemTitle() {
        return title;
    }

    public abstract void getResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize);

}

