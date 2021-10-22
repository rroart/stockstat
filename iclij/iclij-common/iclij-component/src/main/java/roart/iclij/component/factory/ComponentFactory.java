package roart.iclij.component.factory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.pipeline.PipelineConstants;
import roart.constants.IclijConstants;
import roart.iclij.component.AboveBelowComponent;
import roart.iclij.component.Component;
import roart.iclij.component.ComponentMLATR;
import roart.iclij.component.ComponentMLCCI;
import roart.iclij.component.ComponentMLIndicator;
import roart.iclij.component.ComponentMLMACD;
import roart.iclij.component.ComponentMLMulti;
import roart.iclij.component.ComponentMLRSI;
import roart.iclij.component.ComponentMLSTOCH;
import roart.iclij.component.ComponentPredictor;
import roart.iclij.component.ComponentRecommender;
import roart.iclij.component.DatasetComponent;
import roart.iclij.component.FilterComponent;
import roart.iclij.component.ImproveAutoSimulateInvestComponent;
import roart.iclij.component.ImproveSimulateInvestComponent;
import roart.iclij.component.SimulateInvestComponent;

public class ComponentFactory {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new ComponentRecommender();
        case PipelineConstants.PREDICTOR:
            return new ComponentPredictor();
        case PipelineConstants.MLMACD:
            return new ComponentMLMACD();
        case PipelineConstants.MLRSI:
            return new ComponentMLRSI();
        case PipelineConstants.MLATR:
            return new ComponentMLATR();
        case PipelineConstants.MLCCI:
            return new ComponentMLCCI();
        case PipelineConstants.MLSTOCH:
            return new ComponentMLSTOCH();
        case PipelineConstants.MLMULTI:
            return new ComponentMLMulti();
        case PipelineConstants.MLINDICATOR:
            return new ComponentMLIndicator();
        case PipelineConstants.DATASET:
            return new DatasetComponent();
        case PipelineConstants.ABOVEBELOW:
            return new AboveBelowComponent();
        case PipelineConstants.FILTER:
            return new FilterComponent();
        case PipelineConstants.SIMULATEINVEST:
            return new SimulateInvestComponent();
        case PipelineConstants.IMPROVESIMULATEINVEST:
            return new ImproveSimulateInvestComponent();
        case PipelineConstants.IMPROVEAUTOSIMULATEINVEST:
            return new ImproveAutoSimulateInvestComponent();
        default:
            return null;
        }        
    }

    public List<Component> getAllComponents() {
        List<Component> list = new ArrayList<>();
        list.add(new ComponentRecommender());
        list.add(new ComponentPredictor());
        list.add(new ComponentMLMACD());
        list.add(new ComponentMLRSI());
        list.add(new ComponentMLATR());
        list.add(new ComponentMLCCI());
        list.add(new ComponentMLSTOCH());
        list.add(new ComponentMLMulti());
        list.add(new ComponentMLIndicator());

        return list;
    }

}
