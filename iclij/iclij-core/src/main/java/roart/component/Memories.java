package roart.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import roart.action.MarketAction;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.util.MiscUtil;
import roart.service.model.ProfitInputData;

public class Memories {
    
    private Market market;
    
    private Map<Boolean, Map<String, List<Pair<String, String>>>> listComponentMap = new HashMap<>();

    // or make a new object instead of the object array. use this as a pair
    //System.out.println(currentList.get(0).getRecord());
    Map<Triple<String, String, String>, List<MemoryItem>> listMap = new HashMap<>();

    public Memories(Market market) {
        this.market = market;
    }

    public ProfitInputData method(IclijConfig config, MarketAction action) {
        ProfitInputData inputdata;
        inputdata = action.filterMemoryListMapsWithConfidence(market, listMap, config);        
        Map<String, List<Pair<String, String>>> listComponent = createComponentPositionListMap(inputdata.getListMap());
        Map<String, List<Pair<String, String>>> aboveListComponent = createComponentPositionListMap(inputdata.getAboveListMap());
        Map<String, List<Pair<String, String>>> belowListComponent = createComponentPositionListMap(inputdata.getBelowListMap());
        listComponentMap.put(null, listComponent);
        listComponentMap.put(true, aboveListComponent);
        listComponentMap.put(false, belowListComponent);
        return inputdata;
    }
    
    public <T> Map<String, List<Pair<String, String>>> createComponentPositionListMap(Map<Triple<String, String, String>, List<T>> okListMap) {
        Map<String, List<Pair<String, String>>> listComponent = new HashMap<>();
        for (Triple<String, String, String> key : okListMap.keySet()) {
            new MiscUtil().listGetterAdder(listComponent, key.getLeft(), new ImmutablePair<String, String>(key.getMiddle(), key.getRight()));
        }
        return listComponent;
    }

    public void method(List<MemoryItem> currentList) {
        currentList.forEach(m -> new MiscUtil().listGetterAdder(listMap, new ImmutableTriple<String, String, String>(m.getComponent(), m.getSubcomponent(), m.getLocalcomponent()), m));
    }

    public boolean contains(String component, Pair<String, String> pair, Boolean above, MLMetricsItem mlmetrics, boolean useThreshold) {
        if (!useThreshold) {
            return true;
        }
        Map<String, List<Pair<String, String>>> m = listComponentMap.get(above);
        if (m != null) {
            List<Pair<String, String>> n = m.get(component);
            if ( n != null) {
                return n.contains(pair);
            }
        }
        return false;
    }

}
