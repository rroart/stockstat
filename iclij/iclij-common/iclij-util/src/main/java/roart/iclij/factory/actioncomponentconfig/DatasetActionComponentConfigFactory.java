package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.iclij.model.config.ActionComponentConfig;

public class DatasetActionComponentConfigFactory extends ActionComponentConfigFactory{
    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        default:
            return null;
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        return list;
    }


}
