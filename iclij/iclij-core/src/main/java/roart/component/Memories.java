package roart.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.MarketAction;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.util.MiscUtil;
import roart.service.model.ProfitInputData;

public class Memories {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Market market;
    
    private Map<Boolean, Map<String, List<Pair<String, String>>>> aboveMap = new HashMap<>();

    private Map<Boolean, Map<String, List<Pair<String, String>>>> belowMap = new HashMap<>();

    // or make a new object instead of the object array. use this as a pair
    //System.out.println(currentList.get(0).getRecord());
    Map<Triple<String, String, String>, List<MemoryItem>> listMap = new HashMap<>();

    public Memories(Market market) {
        this.market = market;
    }

    public void method(List<MemoryItem> currentList, IclijConfig config, MarketAction action) {
        currentList.forEach(m -> new MiscUtil().listGetterAdder(listMap, new ImmutableTriple<String, String, String>(m.getComponent(), m.getSubcomponent(), m.getLocalcomponent()), m));
        Map[] map = action.filterMemoryListMapsWithConfidence(market, listMap, config);        
        Map<Triple<String, String, String>, Double> aboveThresholdMap = map[0];
        Map<Triple<String, String, String>, Double> belowThresholdMap = map[1];
        Map<Triple<String, String, String>, Double> aboveThresholdAboveMap = map[2];
        Map<Triple<String, String, String>, Double> belowThresholdAboveMap = map[3];
        Map<Triple<String, String, String>, Double> aboveThresholdBelowMap = map[4];
        Map<Triple<String, String, String>, Double> belowThresholdBelowMap = map[5];
        Map<String, List<Pair<String, String>>> aboveThresholdComponentMap = createComponentPositionListMap(aboveThresholdMap);
        Map<String, List<Pair<String, String>>> aboveThresholdAboveComponentMap = createComponentPositionListMap(aboveThresholdAboveMap);
        Map<String, List<Pair<String, String>>> aboveThresholdBelowComponentMap = createComponentPositionListMap(aboveThresholdBelowMap);
        aboveMap.put(null, aboveThresholdComponentMap);
        aboveMap.put(true, aboveThresholdAboveComponentMap);
        aboveMap.put(false, aboveThresholdBelowComponentMap);
        Map<String, List<Pair<String, String>>> belowThresholdComponentMap = createComponentPositionListMap(belowThresholdMap);
        Map<String, List<Pair<String, String>>> belowThresholdAboveComponentMap = createComponentPositionListMap(belowThresholdAboveMap);
        Map<String, List<Pair<String, String>>> belowThresholdBelowComponentMap = createComponentPositionListMap(belowThresholdBelowMap);
        belowMap.put(null, belowThresholdComponentMap);
        belowMap.put(true, belowThresholdAboveComponentMap);
        belowMap.put(false, belowThresholdBelowComponentMap);
    }
    
    public <T> Map<String, List<Pair<String, String>>> createComponentPositionListMap(Map<Triple<String, String, String>, T> okListMap) {
        Map<String, List<Pair<String, String>>> listComponent = new HashMap<>();
        for (Triple<String, String, String> key : okListMap.keySet()) {
            new MiscUtil().listGetterAdder(listComponent, key.getLeft(), new ImmutablePair<String, String>(key.getMiddle(), key.getRight()));
        }
        return listComponent;
    }

    public boolean containsBelow(String component, Pair<String, String> pair, Boolean above, MLMetricsItem mlmetrics, boolean useThreshold) {
        if (!useThreshold) {
            return false;
        }
        Map<String, List<Pair<String, String>>> componentMap = belowMap.get(above);
        if (componentMap != null) {
            List<Pair<String, String>> sublocals = componentMap.get(component);
            if ( sublocals != null) {
                log.info("Found and skipped {}", pair);
                return sublocals.contains(pair);
            }
        }
        return false;
    }

}
