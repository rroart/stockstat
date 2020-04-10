package roart.iclij.service;

import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.iclij.model.WebData;
import roart.iclij.model.WebDataJson;

public class IclijServiceResult {
    private List<String> markets;
    
    private Map<String, String> stocks;
    
    private Map<String, Map<String, Object>> maps;
    
    private String error;
    
    private List<IclijServiceList> lists;

    private IclijConfig iclijConfig;

    //private WebData webdata;

    private WebDataJson webdatajson;

    public IclijServiceResult() {
        super();
    }

    public List<String> getMarkets() {
        return markets;
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

    public IclijConfig getIclijConfig() {
        return iclijConfig;
    }

    public void setIclijConfig(IclijConfig iclijConfig) {
        this.iclijConfig = iclijConfig;
    }
/*
    public WebData getWebdata() {
        return webdata;
    }

    public void setWebdata(WebData webdata) {
        this.webdata = webdata;
    }
    */

    public WebDataJson getWebdatajson() {
        return webdatajson;
    }

    public void setWebdatajson(WebDataJson webdatajson) {
        this.webdatajson = webdatajson;
    }
}
