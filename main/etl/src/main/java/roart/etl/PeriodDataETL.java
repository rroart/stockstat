package roart.etl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.constants.Constants;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;

public class PeriodDataETL {

    /**
     * return perioddata for periodtext, or new one if not existing
     *
     * @param periodDataMap map from periodtext to perioddata
     * @param text periodtext
     * @return perioddata
     */
    
    public PeriodData getPeriodData(Map<String, PeriodData> periodDataMap, String text) {
        PeriodData perioddata = periodDataMap.get(text);
        if (perioddata == null) {
            perioddata = new PeriodData();
            periodDataMap.put(text, perioddata);
        }
        return perioddata;
    }

    /**
     * Creates map from period name to period data
     * it gets the periodtexts from the marketdata
     * creates a pair of (market id, period id)
     * and adds it to the set of pairs in perioddata
     * 
     * @param markets to iterate through
     * @param marketdatamap
     * @return period name map
     */
    
    public Map<String, PeriodData> getPerioddatamap(Set<String> markets,
            Map<String, MarketData> marketdatamap) {
        Map<String, PeriodData> periodDataMap = new HashMap();
        for (String market : markets) {
            String[] periodText = marketdatamap.get(market).periodtext;
            for (int i = 0; i < Constants.PERIODS; i++) {
                String text = periodText[i];
                Pair<String, Integer> pair = new ImmutablePair<String, Integer>(market, i);
                addPairToPeriodDataMap(periodDataMap, text, pair);
            }
            if (true) {
                Pair<String, Integer> pair = new ImmutablePair<String, Integer>(market, Constants.PRICECOLUMN);
                addPairToPeriodDataMap(periodDataMap, Constants.PRICE, pair);                
            }
            if (true) {
                Pair<String, Integer> pair = new ImmutablePair<String, Integer>(market, Constants.INDEXVALUECOLUMN);
                addPairToPeriodDataMap(periodDataMap, Constants.INDEX, pair);                
            }
        }
        return periodDataMap;
    }

    private void addPairToPeriodDataMap(Map<String, PeriodData> periodDataMap, String text,
            Pair<String, Integer> pair) {
        PeriodData perioddata = getPeriodData(periodDataMap, text);
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        pairs.add(pair);
    }

}
