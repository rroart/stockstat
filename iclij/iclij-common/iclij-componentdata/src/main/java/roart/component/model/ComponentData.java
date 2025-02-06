package roart.component.model;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.TimingItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialVolume;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.MapUtil;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;

public class ComponentData {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private ComponentInput input;
    
    private IclijConfig config;
    
    // new
    private ControlService service;
    
    private Map<String, Object> configValueMap;

    // "internal" only, not used in the end
    private Integer category;
    
    private String categoryTitle;
    
    private ComponentTime componentTime = new ComponentTime();
    
    protected PipelineData[] resultMaps;

    protected PipelineData resultMap;

    private List<String> stockDates;
    
    private Map<String, List<List<Double>>> categoryValueMap;
    
    private Map<String, List<List<Double>>> fillCategoryValueMap;
    
    private Map<String, SerialVolume[]> volumeMap;
    
    private Integer usedsec;

    private Map<String, Object> updateMap;
    
    private Map<String, Double> scoreMap;
    
    private String action;
    
    private List<String> disableList = new ArrayList<>();
    
    private List<TimingItem> timings = new ArrayList<>();
    
    private Market market;
    
    public ComponentData() {
        
    }
    
    public ComponentData(ComponentData componentparam) {
        this.config = componentparam.config;
        this.input = componentparam.input;
        this.input.setValuemap(new HashMap<>(componentparam.input.getValuemap()));
        this.service = componentparam.service;
        this.service.conf.getConfigData().setConfigValueMap(new HashMap<>(this.service.conf.getConfigData().getConfigValueMap()));
        this.category = componentparam.category;
        this.categoryTitle = componentparam.categoryTitle;
        this.setBaseDate(componentparam.getBaseDate());
        this.setFutureDate(componentparam.getFutureDate());
        this.setOffset(componentparam.getOffset());
        this.setFuturedays(componentparam.getFuturedays());
        this.resultMap = componentparam.resultMap;
        this.stockDates = componentparam.stockDates;
        this.categoryValueMap = componentparam.categoryValueMap;
        this.fillCategoryValueMap = componentparam.fillCategoryValueMap;
        this.volumeMap = componentparam.volumeMap;
        this.usedsec = componentparam.usedsec;
        this.updateMap = new HashMap<>(); //componentparam.updateMap;
        this.configValueMap = new HashMap<>(this.service.conf.getConfigData().getConfigValueMap());
        this.action = componentparam.action;
        this.disableList = componentparam.disableList;
        this.timings = componentparam.timings;
        this.market = componentparam.market;
    }

    public ComponentData(ComponentInput input) {
        this.input = input;
    }

    public static ComponentData getParam(IclijConfig iclijConfig, ComponentInput input, int days) throws Exception {
        return getParam(iclijConfig, input, days, null, null);
    }

    public static ComponentData getParam(IclijConfig iclijConfig, ComponentInput input, int days, WebFluxUtil webFluxUtil) throws Exception {
        return getParam(iclijConfig, input, days, null, webFluxUtil);
    }

    public static ComponentData getParam(IclijConfig iclijConfig, ComponentInput input, int days, Market aMarket, WebFluxUtil webFluxUtil) throws Exception {
        ComponentData param = new ComponentData(input);
        //param.setAction(IclijConstants.FINDPROFIT);
        String market = input.getConfigData().getMarket();
        String mlmarket = input.getConfigData().getMlmarket();
        param.config = iclijConfig;
        ControlService srv;
        if (webFluxUtil != null) {
            srv = new ControlService(iclijConfig, webFluxUtil);
        } else {
            srv = new ControlService(iclijConfig);
        }
        param.setService(srv);
        if (market != null) {
            srv.conf.getConfigData().setMarket(market);
            param.getInput().setMarket(market);
            srv.conf.getConfigData().setMlmarket(mlmarket);
            param.getInput().setMlmarket(mlmarket);
        }
        // verification days, 0 or something
        param.setOffset(days);
        param.setMarket(aMarket);
        return param;
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

    public IclijConfig getConfig() {
        return config;
    }

    public void setConfig(IclijConfig config) {
        this.config = config;
    }

    public ControlService getService() {
        return service;
    }

    public void setService(ControlService service) {
        this.service = service;
        service.getAndSetCoreConfig();
        this.configValueMap = new HashMap<>(service.conf.getConfigData().getConfigValueMap());
    }

    public Map<String, Object> getConfigValueMap() {
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

    public ComponentTime getComponentTime() {
        return componentTime;
    }

    public void setComponentTime(ComponentTime componentTime) {
        this.componentTime = componentTime;
    }

    public LocalDate getBaseDate() {
        return getComponentTime().getBaseDate();
    }

    public void setBaseDate(LocalDate baseDate) {
        getComponentTime().setBaseDate(baseDate);
    }

    public LocalDate getFutureDate() {
        return getComponentTime().getFutureDate();
    }

    public void setFutureDate(LocalDate futureDate) {
        getComponentTime().setFutureDate(futureDate);
    }

    public Integer getOffset() {
        return getComponentTime().getOffset();
    }

    public void setOffset(Integer offset) {
        getComponentTime().setOffset(offset);
    }

    public Integer getFuturedays() {
        return getComponentTime().getFuturedays();
    }

    public void setFuturedays(Integer futuredays) {
        getComponentTime().setFuturedays(futuredays);
    }

    public PipelineData[] getResultMaps() {
        return resultMaps;
    }

    public void setResultMaps(PipelineData[] resultMaps) {
        this.resultMaps = resultMaps;
    }

    public PipelineData getResultMap() {
        return resultMap;
    }

    public void setResultMap(PipelineData resultMap) {
        this.resultMap = resultMap;
    }

    public List<String> getStockDates() {
        return stockDates;
    }

    public void setStockDates(List<String> stockDates) {
        this.stockDates = stockDates;
    }

    public Map<String, List<List<Double>>> getCategoryValueMap() {
        return categoryValueMap;
    }

    public void setCategoryValueMap(Map<String, List<List<Double>>> categoryValueMap) {
        this.categoryValueMap = categoryValueMap;
    }

    public Map<String, List<List<Double>>> getFillCategoryValueMap() {
        return fillCategoryValueMap;
    }

    public void setFillCategoryValueMap(Map<String, List<List<Double>>> fillCategoryValueMap) {
        this.fillCategoryValueMap = fillCategoryValueMap;
    }

    public Map<String, SerialVolume[]> getVolumeMap() {
        return volumeMap;
    }

    public void setVolumeMap(Map<String, SerialVolume[]> aVolumeMap) {
        this.volumeMap = aVolumeMap;
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

    public List<TimingItem> getTimings() {
        return timings;
    }

    public void setTimings(List<TimingItem> timings) {
        this.timings = timings;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public int setDatesNot() throws ParseException {
        List<String> stockdates = service.getDates(getMarket());
        String date = TimeUtil.convertDate2(this.getInput().getEnddate());
        List<String> list = new TimeUtil().setDates(date, stockdates, getComponentTime().getOffset(), input.getLoopoffset(), getComponentTime().getFuturedays());
        String baseDateStr = list.get(0);
        String futureDateStr = list.get(1);
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        this.setBaseDate(TimeUtil.convertDate(baseDateStr));
        this.setFutureDate(TimeUtil.convertDate(futureDateStr));
        if (stockdates.size() - 1 - getComponentTime().getFuturedays() - getComponentTime().getOffset() < 0) {
            int jj = 0;
        }
        return getComponentTime().getOffset();
    }
    
    public int setDates(String aDate, List<String> stockdates, MarketActionData actionData, Market market) throws ParseException {
        return getComponentTime().setDates(aDate, stockdates, actionData, market, getService(), getInput());
    }
    
    public void setUsedsec(long time0) {
        this.usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
    }
    
    public void getAndSetCategoryValueMap(boolean useMl) {
        getService().conf.getConfigData().setDate(getFutureDate());
        Map<String, Object> setValueMap = new HashMap<>();
	// common
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);

        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNINGPREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MISCTHRESHOLD, null);

	// common
        setValueMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        setValueMap.put(ConfigConstants.MISCINTERPOLATIONLASTNULL, Boolean.TRUE);

        service.conf.getConfigData().setConfigValueMap(new HashMap<>(configValueMap));
        service.conf.getConfigData().getConfigValueMap().putAll(setValueMap);
        PipelineData[] result = getService().getContent(useMl);
        this.resultMaps = result;
        try {
            // TODO null name, fix later
            // TODO needed where, reread?
            List<String> stockdates = PipelineUtils.getDatelist(PipelineUtils.getPipeline(result, this.getCategoryTitle()));
            this.setStockDates(stockdates);
            Map<String, List<List<Double>>> aCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, this.getCategoryTitle()).get(PipelineConstants.LIST)));
            this.setCategoryValueMap(aCategoryValueMap);
            Map<String, List<List<Double>>> aFillCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, this.getCategoryTitle()).get(PipelineConstants.FILLLIST)));
            this.setFillCategoryValueMap(aFillCategoryValueMap);
            Map<String, SerialVolume[]> aVolumeMap = PipelineUtils.getVolume(PipelineUtils.getPipeline(result, this.getCategoryTitle()));
            this.setVolumeMap(aVolumeMap);
        } catch (Exception e) {
            int jj = 0;
            if (this.getCategoryTitle() != null) {
            log.error("Ex", e);
            } else {
                log.error("Ex null cat title");
            }
        }        
    }

    public void getAndSetWantedCategoryValueMap(boolean useMl) {
        getService().conf.getConfigData().setDate(getFutureDate());
        Map<String, Object> setValueMap = new HashMap<>();
	// common
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);

        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNINGPREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MISCTHRESHOLD, null);
        setValueMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        setValueMap.put(ConfigConstants.MISCMYDAYS, 0);

	// common
        setValueMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        setValueMap.put(ConfigConstants.MISCINTERPOLATIONLASTNULL, Boolean.TRUE);
	
        service.conf.getConfigData().setConfigValueMap(new HashMap<>(configValueMap));
        service.conf.getConfigData().getConfigValueMap().putAll(setValueMap);
        PipelineData[] result = getService().getContent(useMl);
        this.resultMaps = result;
        try {
            MyMyConfig conf = null;
            // NOT yet Inmemory inmemory = InmemoryFactory.get(conf .getInmemoryServer(), conf.getInmemoryHazelcast(), conf.getInmemoryRedis());
            //log.info("" + result.keySet());
            //log.info("" + result.get(PipelineConstants.META).keySet());
            String cat = PipelineUtils.getMetaCat(PipelineUtils.getPipeline(result, PipelineConstants.META));
            List<String> stockdates = PipelineUtils.getDatelist(PipelineUtils.getPipeline(result, cat));
            this.setStockDates(stockdates);
            Map<String, List<List<Double>>> aCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, cat).get(PipelineConstants.LIST)));
            this.setCategoryValueMap(aCategoryValueMap);
            Map<String, List<List<Double>>> aFillCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, cat).get(PipelineConstants.FILLLIST)));
            this.setFillCategoryValueMap(aFillCategoryValueMap);
            InmemoryMessage msg = null;
            Map<String, SerialVolume[]> aVolumeMap = PipelineUtils.getVolume(PipelineUtils.getPipeline(result, cat));
            this.setVolumeMap(aVolumeMap);
        } catch (Exception e) {
            int jj = 0;
            log.error("Ex", e);
        }
    }

    public PipelineData getResultMap(String mapName, Map<String, Object> setValueMap, boolean useMl) {
        zerokey(configValueMap);
        service.conf.getConfigData().setConfigValueMap(new HashMap<>(configValueMap));
        zerokey(setValueMap);
        service.conf.getConfigData().getConfigValueMap().putAll(setValueMap);
        if (updateMap != null) {
            zerokey(updateMap);
            service.conf.getConfigData().getConfigValueMap().putAll(updateMap);
        }
        service.conf.getConfigData().setDate(getBaseDate());
        PipelineData[] maps = service.getContent(useMl, getDisableList());
        this.resultMaps = maps;
        //System.out.println(maps.keySet());
        PipelineData aMap = null;
        if (mapName != null) {
            aMap = PipelineUtils.getPipeline(maps, mapName);
        }
        this.resultMap = aMap;
        return aMap;  
    }

    public void setCategory(PipelineData aMap) {
        if (aMap == null) {
            int jj = 0;
            return;
        }
        Integer aCategory = PipelineUtils.getCat(aMap);
        this.setCategory(aCategory);
        String aCategoryTitle = PipelineUtils.getCatTitle(aMap);
        this.setCategoryTitle(aCategoryTitle);        
    }
    
    public void zerokey(Map map) {
        Set keys = map.keySet();
        if (map.get(null) != null) {
            log.info("Keys {}",keys);
            log.info("Value {}", map.get(null));            
        }
    }
}
    
