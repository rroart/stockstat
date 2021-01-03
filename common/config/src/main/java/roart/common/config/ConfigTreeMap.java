package roart.common.config;

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
        ConfigTreeMap firstMap = map.get(first);
        if (firstMap == null) {
            return null;
        }
        return search(firstMap.getConfigTreeMap(), rest);
    }

    public static void insert(Map<String, ConfigTreeMap> map, String name, String fullName, String path, Map<String, Object> deflt) {
        int index = name.indexOf('.');
        if (index == - 1) {
            Boolean enabled = null;
            if (name.contains("[@enable]")) {
                enabled = (Boolean) deflt.get(fullName);
            }
            name = name.replaceFirst("\\[@enable\\]", "");
            if (map.get(name) != null) {
                return;
            }
            ConfigTreeMap newTree = new ConfigTreeMap();
            newTree.setName(fullName);
            newTree.setEnabled(enabled);
            map.put(name, newTree);
            return;
            //return map.get(name);
        }
        String first = name.substring(0, index);
        first = first.replaceFirst("\\[@enable\\]", "");
        String rest = name.substring(index + 1);
        String newPath = first;
        if (!path.isEmpty()) {
            newPath = path + "." + first;
        }
        ConfigTreeMap nextMap = map.get(first);
        if (nextMap == null) {
            Boolean enabled = null;
            if (name.contains("[@enable]")) {
                enabled = (Boolean) deflt.get(fullName);
            }
            name = name.replaceFirst("\\[@enable\\]", "");
            if (map.get(name) != null) {
                return;
            }
            ConfigTreeMap newTree = new ConfigTreeMap();
            newTree.setName(newPath);
            newTree.setEnabled(enabled);
            map.put(first, newTree);            
            nextMap = newTree;
        }
        insert(nextMap.getConfigTreeMap(), rest, fullName, newPath, deflt);
    }

}
