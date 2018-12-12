package roart.service;

import java.util.List;
import java.util.Map;

import roart.config.MyConfig;
import roart.model.ResultItem;

public class ServiceResult {
    private MyConfig config;
    
    private List<String> markets;
    
    private Map<String, String> stocks;
    
    private List<ResultItem> list;
    
    private Map<String, Map<String, Object>> maps;
    
    private String error;

    public ServiceResult() {
        super();
    }

    public MyConfig getConfig() {
        return config;
    }

    public void setConfig(MyConfig config) {
        this.config = config;
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

    public List<ResultItem> getList() {
        return list;
    }

    public void setList(List<ResultItem> list) {
        this.list = list;
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
    
}
