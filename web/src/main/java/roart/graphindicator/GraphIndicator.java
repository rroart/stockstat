package roart.graphindicator;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.GUISize;
import roart.service.ControlService;

public abstract class GraphIndicator {

    protected static Logger log = LoggerFactory.getLogger(GraphIndicator.class);

    protected String title;
    protected ControlService controlService;

    public GraphIndicator(ControlService controlService, String string) {
        this.title = string;
        this.controlService = controlService;
    }

    abstract public boolean isEnabled();

    public String getResultItemTitle() {
        return title;
    }

    abstract public void getResult(List retlist, Set<Pair> ids, GUISize guiSize);

}

