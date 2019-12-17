package roart.evolution.fitness;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractScore {
    protected static Logger log = LoggerFactory.getLogger(AbstractScore.class);
    public abstract double calculateResult(Map<String, List<Double>> list, Double threshold);
    public abstract double[] calculate(Map<String, List<Double>> list, Double threshold);
    public abstract String name();
}
