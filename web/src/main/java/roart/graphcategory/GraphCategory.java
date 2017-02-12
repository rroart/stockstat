package roart.graphcategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.graphindicator.GraphIndicator;
import roart.service.ControlService;

public abstract class GraphCategory {

    protected static Logger log = LoggerFactory.getLogger(GraphCategory.class);

    protected String title;
    protected ControlService controlService;
    protected List<GraphIndicator> indicators = new ArrayList();

    public GraphCategory(ControlService controlService, String periodText) {
        this.controlService = controlService;
        title = periodText;
    }

    abstract public void addResult(List retlist, Set<Pair> ids);
}

