package roart.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigTreeMap {
    public String name = null;
    public Boolean enabled = null;
    public Map<String, ConfigTreeMap> configTreeMap = new HashMap();
}
