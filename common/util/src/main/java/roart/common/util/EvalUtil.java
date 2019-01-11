package roart.common.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EvalUtil {
    protected static Logger log = LoggerFactory.getLogger(EvalUtil.class);
    public abstract double calculateResult(Map<String, List<Double>> list);
    public abstract double[] calculate(Map<String, List<Double>> list);
    public abstract String name();
}
