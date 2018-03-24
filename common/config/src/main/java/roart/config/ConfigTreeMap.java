package roart.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigTreeMap {
    private String name = null;
    
    private Boolean enabled = null;
    
    private Map<String, ConfigTreeMap> configTreeMap = new HashMap<>();

    public ConfigTreeMap() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, ConfigTreeMap> getConfigTreeMap() {
        return configTreeMap;
    }

    public void setConfigTreeMap(Map<String, ConfigTreeMap> configTreeMap) {
        this.configTreeMap = configTreeMap;
    }

    public ConfigTreeMap search(String name) {
        return search(configTreeMap, name);
    }
    
    private ConfigTreeMap search(Map<String, ConfigTreeMap> map, String name) {
        int index = name.indexOf('.');
        if (index == - 1) {
            return map.get(name);
        }
        String first = name.substring(0, index);
        String rest = name.substring(index + 1);
        return search(map.get(first).getConfigTreeMap(), rest);
    }
}
