package roart.iclij.service;

import java.util.List;
import java.util.Map;

import roart.common.config.ConfigData;
import roart.common.model.MetaItem;
import roart.common.pipeline.data.PipelineData;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.WebData;
import roart.iclij.model.WebDataJson;
import roart.result.model.ResultItem;

public class IclijServiceResult {
    private List<String> markets;
    
    private List<MetaItem> metas;
    
    private Map<String, String> stocks;
    
    private Map<String, Map<String, Object>> maps;
    
    private String error;
    
    private List<IclijServiceList> lists;

    private ConfigData configData;

    //private WebData webdata;

    private WebDataJson webdatajson;

    private List<ResultItem> list;
    
    private PipelineData pipelineData;
    
    public IclijServiceResult() {
        super();
    }

    public List<String> getMarkets() {
        return markets;
    }

    public List<MetaItem> getMetas() {
        return metas;
    }

    public void setMetas(List<MetaItem> metas) {
        this.metas = metas;
    }

    public void setMarkets(List<String> markets) {
        this.markets = markets;
    }

    public Map<String, String> getStocks() {
        return stocks;
    }

    public void setStocks(Map<String, String> stocks) {
        this.stocks = stocks;
    }

    public Map<String, Map<String, Object>> getMaps() {
        return maps;
    }

    public void setMaps(Map<String, Map<String, Object>> maps) {
        this.maps = maps;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<IclijServiceList> getLists() {
        return lists;
    }

    public void setLists(List<IclijServiceList> lists) {
        this.lists = lists;
    }

    public WebDataJson getWebdatajson() {
        return webdatajson;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public void setConfigData(ConfigData configData) {
        this.configData = configData;
    }

    public void setWebdatajson(WebDataJson webdatajson) {
        this.webdatajson = webdatajson;
    }

    public List<ResultItem> getList() {
        return list;
    }

    public void setList(List<ResultItem> list) {
        this.list = list;
    }

    public PipelineData getPipelineData() {
        return pipelineData;
    }

    public void setPipelineData(PipelineData pipelineData) {
        this.pipelineData = pipelineData;
    }
    
    
}
