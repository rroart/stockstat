package roart.iclij.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.service.util.MiscUtil;

public class Memories {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Market market;
    
    // above market threshold, both/above/below key -> map of component -> sublocal list
    private Map<Boolean, Map<String, List<Pair<String, String>>>> aboveMap = new HashMap<>();

    // below market threshold
    private Map<Boolean, Map<String, List<Pair<String, String>>>> belowMap = new HashMap<>();

    // or make a new object instead of the object array. use this as a pair
    //System.out.println(currentList.get(0).getRecord());
    Map<Triple<String, String, String>, List<MemoryDTO>> listMap = new HashMap<>();

    public Memories(Market market) {
        this.market = market;
    }

    public void method(List<MemoryDTO> currentList, IclijConfig config) {
        currentList.forEach(m -> new MiscUtil().listGetterAdder(listMap, new ImmutableTriple<String, String, String>(m.getComponent(), m.getSubcomponent(), m.getLocalcomponent()), m));
        Map<Triple<String, String, String>, List<MemoryDTO>> okListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> aboveThresholdMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryDTO>> aboveOkListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> aboveThresholdAboveMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryDTO>> belowOkListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> aboveThresholdBelowMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryDTO>> badListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> belowThresholdMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryDTO>> aboveBadListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> belowThresholdAboveMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryDTO>> belowBadListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> belowThresholdBelowMap = new HashMap<>();
        for(Entry<Triple<String, String, String>, List<MemoryDTO>> entry : listMap.entrySet()) {
            Triple<String, String, String> keys = entry.getKey();
            List<MemoryDTO> memoryList = entry.getValue();
            List<Double> confidences = memoryList.stream().map(MemoryDTO::getConfidence).collect(Collectors.toList());
            confidences = confidences.stream().filter(m1 -> m1 != null && !m1.isNaN()).collect(Collectors.toList());
            List<Double> aboveConfidenceList = new ArrayList<>();
            List<Double> belowConfidenceList = new ArrayList<>();
            if (true) {
            for (MemoryDTO memory : memoryList) {
                // get count and sizes for rightly predicted above or below a threshold
                // and add them to their respective lists
                Long above = memory.getAbovepositives();
                Long below = memory.getBelowpositives();
                Long abovesize = memory.getAbovesize();
                Long belowsize = memory.getBelowsize();
                if (above != null && abovesize !=null && above > 0 && abovesize > 0) {
                    Double aboveConfidence = ( (double ) above) / abovesize;
                    aboveConfidenceList.add(aboveConfidence);
                }
                if (below != null && belowsize !=null && below > 0 && belowsize > 0) {
                    Double belowConfidence = ( (double ) below) / belowsize;
                    belowConfidenceList.add(belowConfidence);
                }
            }
            } else {
                confidences = new ArrayList<>();
                
                Long positives = memoryList.stream().map(MemoryDTO::getPositives).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long size = memoryList.stream().map(MemoryDTO::getSize).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long above = memoryList.stream().map(MemoryDTO::getAbovepositives).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long below = memoryList.stream().map(MemoryDTO::getAbovesize).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long abovesize = memoryList.stream().map(MemoryDTO::getBelowpositives).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long belowsize = memoryList.stream().map(MemoryDTO::getBelowsize).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                if (size != null) {
                    Double confidence = ( (double ) positives) / size;
                    confidences.add(confidence);                    
                }
                if (abovesize != null) {
                    Double aboveConfidence = ( (double ) above) / abovesize;
                    aboveConfidenceList.add(aboveConfidence);                    
                }
                if (belowsize != null) {
                    Double belowConfidence = ( (double ) below) / belowsize;
                    belowConfidenceList.add(belowConfidence);                    
                }
            }
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            Optional<Double> aboveMinOpt = aboveConfidenceList.parallelStream().reduce(Double::min);
            Optional<Double> belowMinOpt = belowConfidenceList.parallelStream().reduce(Double::min);
            handleMin(market, aboveThresholdMap, belowThresholdMap, keys, minOpt);
            handleMin(market, aboveThresholdAboveMap, belowThresholdAboveMap, keys, aboveMinOpt);
            handleMin(market, aboveThresholdBelowMap, belowThresholdBelowMap, keys, belowMinOpt);
        }
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

    public boolean containsBelow(String component, Pair<String, String> pair, Boolean above, MLMetricsDTO mlmetrics, boolean useThreshold) {
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

    private void handleMin(Market market, Map<Triple<String, String, String>, Double> aboveThresholdMap,
            Map<Triple<String, String, String>, Double> belowThresholdMap, Triple<String, String, String> keys,
            Optional<Double> valOpt) {
        if (valOpt.isPresent()) {
            Double val = valOpt.get();
            if (val >= market.getFilter().getConfidence()) {
                aboveThresholdMap.put(keys, val);
            } else {
                belowThresholdMap.put(keys, val);               
            }
        }
    }

}
