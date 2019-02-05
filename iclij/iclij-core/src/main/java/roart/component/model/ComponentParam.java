package roart.component.model;

import java.text.ParseException;
import java.time.LocalDate;
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

public class ComponentParam {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String market;
    
    // "internal" only, not used in the end
    private Integer category;
    
    private String categoryTitle;
    
    private LocalDate baseDate;
    
    private LocalDate futureDate;
    
    private Map<String, List<List<Double>>> categoryValueMap;
    
    private Integer usedsec;

    private boolean doSave;
    
    private boolean doPrint;

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
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

    public boolean isDoSave() {
        return doSave;
    }

    public void setDoSave(boolean doSave) {
        this.doSave = doSave;
    }

    public boolean isDoPrint() {
        return doPrint;
    }

    public void setDoPrint(boolean doPrint) {
        this.doPrint = doPrint;
    }

    public int setDates(ControlService srv, int futuredays, Integer offset, String aDate) throws ParseException {
        List<String> stockdates = srv.getDates(market);
        if (aDate != null) {
            int index = stockdates.indexOf(aDate);
            if (index >= 0) {
                offset = stockdates.size() - index;
            }
        }
        String baseDateStr = stockdates.get(stockdates.size() - 1 - futuredays - offset);
        String futureDateStr = stockdates.get(stockdates.size() - 1 - offset);
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
    
    public void getAndSetCategoryValueMap(ControlService srv) {
        srv.conf.setdate(TimeUtil.convertDate(this.getFutureDate()));
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        srv.conf.getConfigValueMap().putAll(setValueMap);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> aCategoryValueMap = (Map<String, List<List<Double>>>) result.get("" + this.getCategory()).get(PipelineConstants.LIST);
        this.setCategoryValueMap(aCategoryValueMap);
    }

    public Map<String, Object> getResultMap(ControlService srv, String mapName, Map<String, Object> setValueMap) {
        srv.conf.getConfigValueMap().putAll(setValueMap);
        srv.conf.setdate(TimeUtil.convertDate(this.getBaseDate()));
        Map<String, Map<String, Object>> maps = srv.getContent();
        Map<String, Object> aMap = (Map) maps.get(mapName);
        return aMap;  
    }

    public Map<String, Object> getCategoryResultMap(ControlService srv, String mapName, Map<String, Object> setValueMap) throws Exception {
        srv.conf.getConfigValueMap().putAll(setValueMap);
        srv.conf.setdate(TimeUtil.convertDate(this.getBaseDate()));
        Map<String, Map<String, Object>> maps = srv.getContent();
        String wantedCat = ServiceUtil.getWantedCategory(maps, mapName);
        if (wantedCat == null) {
            return null;
        }
        Map aMap = (Map) maps.get(wantedCat).get(mapName);
        return aMap;  
    }

    public void setCategory(Map aMap) {
        Integer aCategory = (Integer) aMap.get(PipelineConstants.CATEGORY);
        this.setCategory(aCategory);
        String aCategoryTitle = (String) aMap.get(PipelineConstants.CATEGORYTITLE);
        this.setCategoryTitle(aCategoryTitle);        
    }
}
    