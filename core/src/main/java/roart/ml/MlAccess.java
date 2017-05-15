package roart.ml;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MlAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract void learntest(Map<double[], Double> map, int modelInt, int size, String period, String mapname, int outcomes);

    public abstract Double eval(int modelInt, String period, String mapname);

    public abstract Map<Integer, Map<String, Double[]>> classify(Map<String, double[]> map, int modelInt, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap);

    public abstract int getSizes();

    public abstract int addTitles(Object[] objs, int retindex, String title, String key);

    public abstract List<Integer> getModels();

    public abstract int addResults(Object[] objs, int retindex, String id, Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> posIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> negIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> commonIdTypeModelMacdMap,
            Map<Integer, Map<String, Double[]>> posIdTypeModelMacdMap,
            Map<Integer, Map<String, Double[]>> negIdTypeModelMacdMap);
    
        
}

