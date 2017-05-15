package roart.ml;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import roart.config.ConfigConstants;
import roart.model.StockItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MlDao {
	private static Logger log = LoggerFactory.getLogger(MlDao.class);

	private MlAccess access = null;

	public MlDao(String instance) {
	    instance(instance);
	}
	
	private void instance(String type) {
		System.out.println("instance " + type);
		log.info("instance " + type);
		if (type == null) {
			return;
		}
		// TODO temp fix
		if (true || access == null) {
			if (type.equals(ConfigConstants.SPARK)) {
				access = new MlSparkAccess();
				//new MlSpark();
			}
			if (type.equals(ConfigConstants.TENSORFLOW)) {
				access = new MlTensorflowAccess();
			}
		}
	}

    public void learntest(Map<double[], Double> map, int modelInt, int size, String period, String mapname, int outcomes) {
        long time1 = System.currentTimeMillis();
        access.learntest(map, modelInt, size, period, mapname, outcomes);
        log.info("time " + modelInt + " " + period + " " + mapname + " " + (System.currentTimeMillis() - time1));
    }

    public Double eval(int modelInt, String period, String mapname) {
        return access.eval(modelInt, period, mapname);
    }

    public Map<Integer, Map<String, Double[]>> classify(Map<String, double[]> map, int modelInt, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        long time1 = System.currentTimeMillis();
         Map<Integer, Map<String, Double[]>> result = access.classify(map, modelInt, size, period, mapname, outcomes, shortMap);
        log.info("time " + modelInt + " " + period + " " + mapname + " " + (System.currentTimeMillis() - time1));
        return result;
            }

    public int getSizes() {
        return access.getSizes();
    }

    public int addTitles(Object[] objs, int retindex, String title, String key) {
        return access.addTitles(objs, retindex, title, key);
    }

    public List<Integer> getModels() {
            return access.getModels();
    }

    public int addResults(Object[] objs, int retindex, String id, Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> posIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> negIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> commonIdTypeModelMacdMap,
            Map<Integer, Map<String, Double[]>> posIdTypeModelMacdMap,
            Map<Integer, Map<String, Double[]>> negIdTypeModelMacdMap) {
         return access.addResults(objs, retindex, id,
                        commonIdTypeModelHistMap,
                        posIdTypeModelHistMap,
                        negIdTypeModelHistMap,
                        commonIdTypeModelMacdMap,
                        posIdTypeModelMacdMap,
                        negIdTypeModelMacdMap);
    }

}
