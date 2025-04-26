package roart.common.service;

import java.util.List;
import java.util.Map;

import roart.common.model.MetaDTO;
import roart.iclij.config.IclijConfig;
import roart.result.model.ResultItem;

public class ServiceResult {
    private IclijConfig config;
    
    private List<String> markets;
    
    private List<MetaDTO> metas;
    
    private Map<String, String> stocks;
    
    private List<ResultItem> list;
    
    private Map<String, Map<String, Object>> maps;
    
    private String error;

    public ServiceResult() {
        super();
    }

    public IclijConfig getConfig() {
        return config;
    }

    public void setConfig(IclijConfig config) {
        this.config = config;
    }

    public List<String> getMarkets() {
        return markets;
    }

    public void setMarkets(List<String> markets) {
        this.markets = markets;
    }

    public List<MetaDTO> getMetas() {
        return metas;
    }

    public void setMetas(List<MetaDTO> metas) {
        this.metas = metas;
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
