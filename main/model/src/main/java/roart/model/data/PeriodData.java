package roart.model.data;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class PeriodData {

    public String date0;
    public String date1;
    public Set<Pair<String, Integer>> pairs = new HashSet<>();
    
}
