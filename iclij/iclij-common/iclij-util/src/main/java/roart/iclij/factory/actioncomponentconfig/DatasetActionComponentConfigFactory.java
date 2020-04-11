package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.model.config.DatasetConfig;

public class DatasetActionComponentConfigFactory extends ActionComponentConfigFactory{
    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        default:
            return new DatasetConfig();
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new DatasetConfig());
        return list;
    }


}
