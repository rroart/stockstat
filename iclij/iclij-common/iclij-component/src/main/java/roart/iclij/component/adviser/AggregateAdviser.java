package roart.iclij.component.adviser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import roart.common.util.MapUtil;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public class AggregateAdviser extends Adviser {

    public AggregateAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
        
    }
    
    @Override
    public List<String> getIncs(String aParameter, int buytop, int indexOffset, List<String> stockDates,
            List<String> excludes) {
        List<String> buys = (List<String>) object;
        Map<String, Long> l = buys.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        Map<Long, List<String>> newRetMap = new HashMap<>();
        for (Entry<String, Long> entry : l.entrySet()) {
            MapUtil.mapAddMe(newRetMap, entry.getValue(), entry.getKey());
        }
        List<Long> keys = new ArrayList(new HashSet<>(l.values()));
        Collections.sort(keys);
        Collections.reverse(keys);
        List<String> newl = new ArrayList<>();
        for (Long key : keys) {
            List<String> list = newRetMap.get(key);
            for (int i = 0; i < list.size() && buytop > 0; i++) {
                newl.add(list.get(i));
                buytop--;
            }
        }
        return newl;
    }

    @Override
    public List<String> getParameters() {
        return null;
    }

    @Override
    public void getValueMap(List<String> stockDates, int firstidx2, int lastidx2,
            Map<String, List<List<Double>>> categoryValueMap) {
    }

}
