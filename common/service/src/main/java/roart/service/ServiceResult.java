package roart.service;

import java.util.List;
import java.util.Map;

import roart.config.MyConfig;
import roart.model.ResultItem;

public class ServiceResult {
    public MyConfig config;
    public List<String> markets;
    public Map<String, String> stocks;
    public List<ResultItem> list;
    public Map<String, Map<String, Object>> maps;
    public String error;
}
