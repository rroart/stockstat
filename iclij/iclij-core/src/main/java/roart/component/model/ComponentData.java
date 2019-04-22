package roart.component.model;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.service.ControlService;
import roart.util.ServiceUtil;

public class ComponentData {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private ComponentInput input;
    
    // new
    private ControlService service;
    
    private Map<String, Object> configValueMap;

    // "internal" only, not used in the end
    private Integer category;
    
    private String categoryTitle;
    
    private LocalDate baseDate;
    
    private LocalDate futureDate;

    private Integer offset;
    
    private Integer futuredays;
    
    protected Map<String, Map<String, Object>> resultMaps;

    protected Map<String, Object> resultMap;

    private Map<String, List<List<Double>>> categoryValueMap;
    
    private Integer usedsec;

    private Map<String, Object> updateMap;
    
    private Map<String, Double> scoreMap;
    
    private String action;
    
    private List<String> disableList = new ArrayList<>();
    
    public ComponentData() {
        
    }
    
    public ComponentData(ComponentData componentparam) {
        this.input = componentparam.input;
        this.service = componentparam.service;
        this.category = componentparam.category;
        this.categoryTitle = componentparam.categoryTitle;
        this.baseDate = componentparam.baseDate;
        this.futureDate = componentparam.futureDate;
        this.offset = componentparam.offset;
        this.futuredays = componentparam.futuredays;
        this.resultMap = componentparam.resultMap;
        this.categoryValueMap = componentparam.categoryValueMap;
        this.usedsec = componentparam.usedsec;
        this.updateMap = new HashMap<>(); //componentparam.updateMap;
        this.configValueMap = new HashMap<>(this.service.conf.getConfigValueMap());
        this.action = componentparam.action;
        this.disableList = componentparam.disableList;
    }

    public ComponentData(ComponentInput input) {
        this.input = input;
    }

    public ComponentInput getInput() {
        return input;
    }

    public void setInput(ComponentInput input) {
        this.input = input;
    }

    public String getMarket() {
        return input.getMarket();
    }

    public Integer getLoopoffset() {
        return input.getLoopoffset();
    }

    public boolean isDoSave() {
        return input.isDoSave();
    }

    public boolean isDoPrint() {
        return input.isDoPrint();
    }

    public ControlService getService() {
        return service;
    }

    public void setService(ControlService service) {
        this.service = service;
        service.getConfig();
        this.configValueMap = new HashMap<>(service.conf.getConfigValueMap());
    }

    protected Map<String, Object> getConfigValueMap() {
        return configValueMap;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public LocalDate getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(LocalDate baseDate) {
        this.baseDate = baseDate;
    }

    public LocalDate getFutureDate() {
        return futureDate;
    }

    public void setFutureDate(LocalDate futureDate) {
        this.futureDate = futureDate;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getFuturedays() {
        return futuredays;
    }

    public void setFuturedays(Integer futuredays) {
        this.futuredays = futuredays;
    }

    public Map<String, Map<String, Object>> getResultMaps() {
        return resultMaps;
    }

    public void setResultMaps(Map<String, Map<String, Object>> resultMaps) {
        this.resultMaps = resultMaps;
    }

    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

    public Map<String, List<List<Double>>> getCategoryValueMap() {
        return categoryValueMap;
    }

    public void setCategoryValueMap(Map<String, List<List<Double>>> categoryValueMap) {
        this.categoryValueMap = categoryValueMap;
    }

    public Integer getUsedsec() {
        return usedsec;
    }

    public void setUsedsec(Integer usedsec) {
        this.usedsec = usedsec;
    }

    public Map<String, Object> getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map<String, Object> updateMap) {
        this.updateMap = updateMap;
    }

    public Map<String, Double> getScoreMap() {
        return scoreMap;
    }

    public void setScoreMap(Map<String, Double> scoreMap) {
        this.scoreMap = scoreMap;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<String> getDisableList() {
        return disableList;
    }

    public void setDisableList(List<String> disableList) {
        this.disableList = disableList;
    }

    public int setDates(int futuredaysNot, Integer offsetNot, String aDate) throws ParseException {
        List<String> stockdates = service.getDates(getMarket());
	String date = aDate;
        if (date != null) {
            int index = stockdates.indexOf(date);
            if (index < 0) {
                date = null;
            }
        }
        if (date == null) {
            if (stockdates.isEmpty()) {
                int jj = 0;
            }
            date = stockdates.get(stockdates.size() - 1);
        }
        int dateoffset = 0;
        if (date != null) {
            int index = stockdates.indexOf(date);
            if (index >= 0) {
                dateoffset = stockdates.size() - 1 - index;
            }
        }
        String baseDateStr = stockdates.get(stockdates.size() - 1 - futuredays - dateoffset - offset - input.getLoopoffset());
        String futureDateStr = stockdates.get(stockdates.size() - 1 - dateoffset - offset - input.getLoopoffset());
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        this.baseDate = TimeUtil.convertDate(baseDateStr);
        this.futureDate = TimeUtil.convertDate(futureDateStr);
        if (stockdates.size() - 1 - futuredays - offset < 0) {
            int jj = 0;
        }
        return offset;
    }
    
    public void setUsedsec(long time0) {
        this.usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
    }
    
    public void getAndSetCategoryValueMap() {
        getService().conf.setdate(TimeUtil.convertDate(this.getFutureDate()));
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        service.conf.setConfigValueMap(new HashMap<>(configValueMap));
        service.conf.getConfigValueMap().putAll(setValueMap);
        Map<String, Map<String, Object>> result = getService().getContent();
        this.resultMaps = result;
        try {
        Map<String, List<List<Double>>> aCategoryValueMap = (Map<String, List<List<Double>>>) result.get("" + this.getCategory()).get(PipelineConstants.LIST);
        this.setCategoryValueMap(aCategoryValueMap);
        } catch (Exception e) {
            int jj = 0;
        }
    }

    public Map<String, Object> getResultMap(String mapName, Map<String, Object> setValueMap) {
        service.conf.setConfigValueMap(new HashMap<>(configValueMap));
        service.conf.getConfigValueMap().putAll(setValueMap);
        if (updateMap != null) {
            service.conf.getConfigValueMap().putAll(updateMap);
        }
        service.conf.setdate(TimeUtil.convertDate(this.getBaseDate()));
        Map<String, Map<String, Object>> maps = service.getContent();
        this.resultMaps = maps;
        Map<String, Object> aMap = (Map) maps.get(mapName);
        this.resultMap = aMap;
        return aMap;  
    }

    // ?
    @Deprecated
    public Map<String, Object> getCategoryResultMap(ControlService srv, String mapName, Map<String, Object> setValueMap) throws Exception {
        srv.conf.getConfigValueMap().putAll(setValueMap);
        srv.conf.setdate(TimeUtil.convertDate(this.getBaseDate()));
        service.conf.setConfigValueMap(new HashMap<>(configValueMap));
        Map<String, Map<String, Object>> maps = srv.getContent();
        String wantedCat = ServiceUtil.getWantedCategory(maps, mapName);
        if (wantedCat == null) {
            return null;
        }
        Map aMap = (Map) maps.get(wantedCat).get(mapName);
        return aMap;  
    }

    public void setCategory(Map aMap) {
        if (aMap == null) {
            int jj = 0;
        }
        Integer aCategory = (Integer) aMap.get(PipelineConstants.CATEGORY);
        this.setCategory(aCategory);
        String aCategoryTitle = (String) aMap.get(PipelineConstants.CATEGORYTITLE);
        this.setCategoryTitle(aCategoryTitle);        
    }
}
    
