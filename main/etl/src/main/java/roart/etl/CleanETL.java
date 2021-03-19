package roart.etl;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import roart.common.pipeline.PipelineConstants;

public class CleanETL {

    public void fixmap(Object o) {
        //System.out.println("" + i + " " + o.hashCode());
        Map<String, Object> m = (Map<String, Object>) o;
        Set<String> k = m.keySet();
        m.remove(PipelineConstants.MARKETOBJECT);
        m.remove(PipelineConstants.MARKETCALCULATED);
        m.remove(PipelineConstants.MARKETRESULT);
        m.remove(PipelineConstants.DATAREADER);
        m.remove(PipelineConstants.EXTRAREADER);
        m.remove(PipelineConstants.TRUNCLIST);
        m.remove(PipelineConstants.TRUNCFILLLIST);
        m.remove(PipelineConstants.BASE100LIST);
        m.remove(PipelineConstants.BASE100FILLLIST);
        m.remove(PipelineConstants.TRUNCBASE100LIST);
        m.remove(PipelineConstants.TRUNCBASE100FILLLIST);
        for (Entry<String, Object> e : m.entrySet()) {
            Object value = e.getValue();
            if (value instanceof Map) {
                //System.out.println("" + i + " " + e.getKey() + " " + value.hashCode());
                fixmap((Map<String, Object>) value);
            } else {
                if (value == null) {
                    //System.out.println("" + i + " " + e.getKey() + " " + null);
                    //System.out.println(" v " + null);
                }
            }
        }
        System.out.println("Removed");
    }

}
