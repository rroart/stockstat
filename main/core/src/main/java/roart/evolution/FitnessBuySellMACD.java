package roart.evolution;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.aggregatorindicator.impl.MACDRecommend;
import roart.iclij.config.IclijConfig;
import roart.evolution.chromosome.AbstractChromosome;

@Deprecated
public class FitnessBuySellMACD {
    IclijConfig conf;
    Map<Integer, Map<String, Double[]>> dayMomMap;
    List<Double>[] macdMinMax;
    Map<Integer, Map<String, Double[]>> dayRsiMap;
    List<Double>[] rsiMinMax;
    int macdlen;
    int listlen;
    Map<String, Double[]> listMap;
    AbstractChromosome recommend;
    
    public FitnessBuySellMACD(IclijConfig conf, Map<Integer, Map<String, Double[]>>dayMomMap, List<Double>[] macdMinMax, Map<Integer, Map<String, Double[]>> dayRsiMap, List<Double>[] rsiMinMax, Map<String, Double[]> listMap, AbstractChromosome recommend) {
        this.conf = conf;
        this.macdlen = conf.getTableDays();
        this.listlen = conf.getTableDays();
        this.macdMinMax = macdMinMax;
        this.dayMomMap = dayMomMap;
        this.rsiMinMax = rsiMinMax;
        this.dayRsiMap = dayRsiMap;
        this.listMap = listMap;
        this.recommend = recommend;
    }
    
    
}
