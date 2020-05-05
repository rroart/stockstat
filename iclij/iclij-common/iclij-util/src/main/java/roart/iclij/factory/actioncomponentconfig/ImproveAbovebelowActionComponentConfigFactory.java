package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.iclij.model.config.AbovebelowConfig;
import roart.iclij.model.config.ActionComponentConfig;

public class ImproveAbovebelowActionComponentConfigFactory extends ActionComponentConfigFactory {

    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        default:
            return new AbovebelowConfig();
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new AbovebelowConfig());
        return list;
    }

}
