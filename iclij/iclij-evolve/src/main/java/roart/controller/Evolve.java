package roart.controller;

import java.util.Map;

import roart.common.util.JsonUtil;

public class Evolve {

    public void method(String param) {
        Map<String, Object> map = JsonUtil.convert(param, Map.class);
        map.keySet();
    }
}
