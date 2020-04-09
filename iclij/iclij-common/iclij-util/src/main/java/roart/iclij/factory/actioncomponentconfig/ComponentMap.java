package roart.iclij.factory.actioncomponentconfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import roart.iclij.model.config.ActionComponentConfig;

public class ComponentMap {

    public Map<String, ActionComponentConfig> getComponentMap(Collection<String> listComponent, String action) {
        Map<String, ActionComponentConfig> componentMap = new HashMap<>();
        for (String componentName : listComponent) {
            ActionComponentConfig component = ActionComponentConfigFactory.factoryfactory(action).factory(componentName);
            componentMap.put(componentName, component);
        }
        return componentMap;
    }
    
}
