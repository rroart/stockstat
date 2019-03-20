package roart.component.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.util.ServiceUtil;

public class PredictorData extends ComponentData {
    
    public PredictorData() {
        
    }
    
    public PredictorData(ComponentData param) {
        super(param);
    }
    
    @Override
    public Map<String, Object> getResultMap(String mapName, Map<String, Object> setValueMap) {
        getService().conf.setConfigValueMap(new HashMap<>(getConfigValueMap()));
        getService().conf.getConfigValueMap().putAll(setValueMap);
        if (getUpdateMap() != null) {
            getService().conf.getConfigValueMap().putAll(getUpdateMap());
        }
        getService().conf.setdate(TimeUtil.convertDate(this.getBaseDate()));
        Map<String, Map<String, Object>> maps = getService().getContent();
        
        // this part is different
        String wantedCat = null;
        try {
            wantedCat = ServiceUtil.getWantedCategory2(maps, PipelineConstants.LSTM);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (wantedCat == null) {
            return null;
        }
        Map map = (Map) maps.get(wantedCat + " " + PipelineConstants.LSTM.toUpperCase());
        maps = map;
        // end of part
        
        this.resultMaps = maps;
        Map<String, Object> aMap = map; // another diff (Map) maps.get(mapName);
        this.resultMap = aMap;
        return aMap;  
    }

}
