package roart.component.model;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.TimingDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialVolume;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.MapUtil;
import roart.common.util.TimeUtil;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;
import roart.model.io.IO;

public class ComponentData {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private String id;
    
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
    
    private List<TimingDTO> timings = new ArrayList<>();
    
    private Market market;
    
    private boolean disableCache;
    
    private boolean keepPipeline;
    
    public ComponentData() {
        
    }

    /*
      Basically a copy
      TODO input.valueMap setting is meaningless
      TODO service.coremlconf.configdata.configvaluemap setting is meaningless
      configValueMap is set from service.coremlconf.configdata.configvaluemap
      updateMap is new Map
     */
    
    public ComponentData(ComponentData componentparam) {
        this.id = componentparam.id;
        this.config = componentparam.config;
        this.input = componentparam.input;
        this.input.setValuemap(new HashMap<>(componentparam.input.getValuemap()));
        this.service = componentparam.service;
        this.service.coremlconf.getConfigData().setConfigValueMap(new HashMap<>(this.service.coremlconf.getConfigData().getConfigValueMap()));
        this.category = componentparam.category;
        this.categoryTitle = componentparam.categoryTitle;
        this.setBaseDate(componentparam.getBaseDate());
        this.setFutureDate(componentparam.getFutureDate());
        this.setOffset(componentparam.getOffset());
        this.setFuturedays(componentparam.getFuturedays());
        this.resultMap = componentparam.resultMap;
        this.resultMaps = componentparam.resultMaps;
        this.stockDates = componentparam.stockDates;
        this.categoryValueMap = componentparam.categoryValueMap;
        this.fillCategoryValueMap = componentparam.fillCategoryValueMap;
        this.volumeMap = componentparam.volumeMap;
        this.usedsec = componentparam.usedsec;
        this.updateMap = new HashMap<>(); //componentparam.updateMap;
        this.configValueMap = new HashMap<>(this.service.coremlconf.getConfigData().getConfigValueMap());
        this.action = componentparam.action;
        this.disableList = componentparam.disableList;
        this.timings = componentparam.timings;
        this.market = componentparam.market;
        this.disableCache = componentparam.disableCache;
        this.keepPipeline = componentparam.keepPipeline;
    }

    public ComponentData(ComponentInput input) {
        this.input = input;
    }

    public static ComponentData getParam(IclijConfig iclijConfig, ComponentInput input, int days, IO io) throws Exception {
        return getParam(iclijConfig, input, days, null, io);
    }

    public static ComponentData getParam(IclijConfig iclijConfig, ComponentInput input, int days, Market aMarket, IO io) throws Exception {
        ComponentData param = new ComponentData(input);
        if (iclijConfig.wantsInmemoryPipeline()) {
            String uuid = UUID.randomUUID().toString();
            param.setId(uuid);
        }
        //param.setAction(IclijConstants.FINDPROFIT);
        String market = input.getConfigData().getMarket();
        String mlmarket = input.getConfigData().getMlmarket();
        param.config = iclijConfig;
        ControlService srv = new ControlService(iclijConfig, io);
        param.setService(srv);
        if (market != null) {
            srv.coremlconf.getConfigData().setMarket(market);
            param.getInput().setMarket(market);
            srv.coremlconf.getConfigData().setMlmarket(mlmarket);
            param.getInput().setMlmarket(mlmarket);
        }
        // verification days, 0 or something
        param.setOffset(days);
        param.setMarket(aMarket);
        return param;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        this.configValueMap = new HashMap<>(service.coremlconf.getConfigData().getConfigValueMap());
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

    public List<TimingDTO> getTimings() {
        return timings;
    }

    public void setTimings(List<TimingDTO> timings) {
        this.timings = timings;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public boolean isDisableCache() {
        return disableCache;
    }

    public void setDisableCache(boolean disableCache) {
        this.disableCache = disableCache;
    }

    public boolean isKeepPipeline() {
        return keepPipeline;
    }

    public void setKeepPipeline(boolean keepPipeline) {
        this.keepPipeline = keepPipeline;
    }

    public int setDatesNot() throws ParseException {
        List<String> stockdates = service.getDates(getMarket(), null);
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
        return getComponentTime().setDates(aDate, stockdates, actionData, market, getService(), getInput(), id);
    }
    
    public void setUsedsec(long time0) {
        this.usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
    }
    
    // blae
    public void getAndSetCategoryValueMap(boolean useMl) {
        getService().coremlconf.getConfigData().setDate(getFutureDate());
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

        service.coremlconf.getConfigData().setConfigValueMap(new HashMap<>(configValueMap));
        service.coremlconf.getConfigData().getConfigValueMap().putAll(setValueMap);
        PipelineData[] result = getService().getContent(id, useMl, disableCache, keepPipeline);
        this.resultMaps = result;
        try {
            // TODO null name, fix later
            // TODO needed where, reread?
            Inmemory inmemory = getService().getIo().getInmemoryFactory().get(config);
            List<String> stockdates = PipelineUtils.getDatelist(PipelineUtils.getPipeline(result, this.getCategoryTitle(), inmemory));
            log.info("Category title {}", this.getCategoryTitle());
            this.setStockDates(stockdates);
            Map<String, List<List<Double>>> aCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, this.getCategoryTitle(), inmemory).get(PipelineConstants.LIST)));
            this.setCategoryValueMap(aCategoryValueMap);
            Map<String, List<List<Double>>> aFillCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, this.getCategoryTitle(), inmemory).get(PipelineConstants.FILLLIST)));
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

    public void getAndSetCategoryValueMapAlt() {
        PipelineData[] result = this.resultMaps;
        try {
            // TODO null name, fix later
            // TODO needed where, reread?
            Inmemory inmemory = getService().getIo().getInmemoryFactory().get(config);
            List<String> stockdates = PipelineUtils.getDatelist(PipelineUtils.getPipeline(result, this.getCategoryTitle(), inmemory));
            log.info("Category title {}", this.getCategoryTitle());
            log.info("stockdates {}", stockdates);
            this.setStockDates(stockdates);
            Map<String, List<List<Double>>> aCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, this.getCategoryTitle(), inmemory).get(PipelineConstants.LIST)));
            this.setCategoryValueMap(aCategoryValueMap);
            Map<String, List<List<Double>>> aFillCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, this.getCategoryTitle(), inmemory).get(PipelineConstants.FILLLIST)));
            this.setFillCategoryValueMap(aFillCategoryValueMap);
            Map<String, SerialVolume[]> aVolumeMap = PipelineUtils.getVolume(PipelineUtils.getPipeline(result, this.getCategoryTitle()));
            this.setVolumeMap(aVolumeMap);
        } catch (Exception e) {
            int jj = 0;
            if (this.getCategoryTitle() != null) {
            log.error("Ex", e);
            } else {
                log.error("Ex null cat title", e);
            }
        }        
    }

    // blae
    public void getAndSetWantedCategoryValueMap(boolean useMl) {
        getService().coremlconf.getConfigData().setDate(getFutureDate());
        Map<String, Object> setValueMap = new HashMap<>();
	// common
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);

        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNINGPREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MISCTHRESHOLD, null);

	// different
	setValueMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        setValueMap.put(ConfigConstants.MISCMYDAYS, 0);

	// common
        setValueMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        setValueMap.put(ConfigConstants.MISCINTERPOLATIONLASTNULL, Boolean.TRUE);
	
        service.coremlconf.getConfigData().setConfigValueMap(new HashMap<>(configValueMap));
        service.coremlconf.getConfigData().getConfigValueMap().putAll(setValueMap);
        PipelineData[] result = getService().getContent(id, useMl, disableCache, keepPipeline);
        this.resultMaps = result;
        try {
            //log.info("" + result.keySet());
            //log.info("" + result.get(PipelineConstants.META).keySet());
            Inmemory inmemory = getService().getIo().getInmemoryFactory().get(config);
            String cat = PipelineUtils.getMetaCat(PipelineUtils.getPipeline(result, PipelineConstants.META, inmemory));
            log.info("Category title {} {}", this.getCategoryTitle(), cat);
            List<String> stockdates = PipelineUtils.getDatelist(PipelineUtils.getPipeline(result, cat, inmemory));
            this.setStockDates(stockdates);
            Map<String, List<List<Double>>> aCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, cat, inmemory).get(PipelineConstants.LIST)));
            this.setCategoryValueMap(aCategoryValueMap);
            Map<String, List<List<Double>>> aFillCategoryValueMap = MapUtil.convertA2L(PipelineUtils.sconvertMapDD(PipelineUtils.getPipeline(result, cat, inmemory).get(PipelineConstants.FILLLIST)));
            this.setFillCategoryValueMap(aFillCategoryValueMap);

            Map<String, SerialVolume[]> aVolumeMap = PipelineUtils.getVolume(PipelineUtils.getPipeline(result, cat, inmemory));
            this.setVolumeMap(aVolumeMap);
        } catch (Exception e) {
            int jj = 0;
            log.error("Ex", e);
        }
    }

    public PipelineData getResultMap(String mapName, Map<String, Object> setValueMap, boolean useMl, boolean keepPipeline) {
        zerokey(configValueMap);
        service.coremlconf.getConfigData().setConfigValueMap(new HashMap<>(configValueMap));
        zerokey(setValueMap);
        service.coremlconf.getConfigData().getConfigValueMap().putAll(setValueMap);
        if (updateMap != null) {
            zerokey(updateMap);
            service.coremlconf.getConfigData().getConfigValueMap().putAll(updateMap);
        }
        service.coremlconf.getConfigData().setDate(getBaseDate());
        PipelineData[] maps = service.getContent(id, useMl, getDisableList(), disableCache, keepPipeline);
        this.resultMaps = maps;
        //System.out.println(maps.keySet());
        PipelineData aMap = null;
        if (mapName != null) {
            Inmemory inmemory = getService().getIo().getInmemoryFactory().get(config);
            aMap = PipelineUtils.getPipeline(maps, mapName, inmemory);
        }
        log.info("mapnamer" + mapName + " " + (aMap != null));
        this.resultMap = aMap;
        return aMap;  
    }

    public void setCategory(PipelineData aMap) {
        if (aMap == null) {
            log.error("Map null");
            int jj = 0;
            return;
        }
        Integer aCategory = PipelineUtils.getCat(aMap);
        this.setCategory(aCategory);
        String aCategoryTitle = PipelineUtils.getCatTitle(aMap);
        this.setCategoryTitle(aCategoryTitle);        
        log.info("Category title {}", this.getCategoryTitle());
}
    
    public void zerokey(Map map) {
        Set keys = map.keySet();
        if (map.get(null) != null) {
            log.info("Keys {}",keys);
            log.info("Value {}", map.get(null));            
        }
    }
}
    
