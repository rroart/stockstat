package roart.graphcategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.graphindicator.GraphIndicator;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.service.ControlService;

public abstract class GraphCategory {

    protected static Logger log = LoggerFactory.getLogger(GraphCategory.class);

    protected String title;
    protected IclijConfig conf;
    protected List<GraphIndicator> indicators = new ArrayList<>();
    protected List<AbstractPredictor> predictors = new ArrayList<>();

    public GraphCategory(IclijConfig conf, String periodText) {
        this.conf = conf;
        title = periodText;
    }

    abstract public void addResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize);
}

