package roart.graphcategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.graphindicator.GraphIndicator;
import roart.model.GUISize;
import roart.model.ResultItem;
import roart.service.ControlService;

public abstract class GraphCategory {

    protected static Logger log = LoggerFactory.getLogger(GraphCategory.class);

    protected String title;
    protected MyConfig conf;
    protected List<GraphIndicator> indicators = new ArrayList();

    public GraphCategory(MyConfig conf, String periodText) {
        this.conf = conf;
        title = periodText;
    }

    abstract public void addResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize);
}

