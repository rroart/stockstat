package roart.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import roart.config.IclijConfig;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.config.ConfigConstants;
import roart.db.IclijDbDao;
import roart.model.MemoryItem;
import roart.pipeline.PipelineConstants;

public class UpdateDBAction extends Action {

    private List<String> getComponents() {
        List<String> components = new ArrayList<>();
        components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        components.add(PipelineConstants.PREDICTORSLSTM);
        components.add(PipelineConstants.MLMACD);
        components.add(PipelineConstants.MLINDICATOR);
        return components;
    }

    @Override
    public void goal() throws InterruptedException {
        //if (true) return;
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        //instance.
        List<MemoryItem> memory = null;
        try {
            memory = IclijDbDao.getAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<MemoryItem> toCheck = new ArrayList<>();
        List<Market> markets = null;
        try { 
            markets = instance.getMarkets();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (Market market : markets) {
            List<MemoryItem> marketMemory = null;
            try {
                marketMemory = IclijDbDao.getAll(market.getMarket());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (marketMemory == null) {
                System.out.println("Marketmemory null for " + market.getMarket());
                //continue;
            }
            for (String component : getComponents()) {
                List<MemoryItem> marketComponents = marketMemory.stream().filter(m -> component.equals(m.getComponent())).collect(Collectors.toList());
                Collections.sort(marketComponents, (o1, o2) -> (o2.getRecord().compareTo(o1.getRecord())));
                MemoryItem last = marketComponents.get(0);
                int time = (int) ((System.currentTimeMillis() - last.getRecord().getTime()) / 1000);
                if (time > market.getTime() * 86400) {
                    toCheck.add(last);
                }
            }
        }
        for (MemoryItem memoryItem : toCheck) {
            if (memoryItem.getUsedsec() == null) {
                memoryItem.setUsedsec(0);
            }
        }
        Collections.sort(toCheck, (o1, o2) -> (o2.getUsedsec().compareTo(o1.getUsedsec())));
        for (MemoryItem memoryItem : toCheck) {
            String market = memoryItem.getMarket();
            System.out.println("Will update " + market + " " + memoryItem.getComponent() + " from " + memoryItem.getRecord() +  " with time spent " + memoryItem.getUsedsec());
            switch (memoryItem.getComponent()) {
            case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
                MainAction.goals.add(new ServiceAction(market, ServiceAction.Task.RECOMMENDER));
                break;
            case PipelineConstants.PREDICTORSLSTM:
                MainAction.goals.add(new ServiceAction(market, ServiceAction.Task.PREDICTOR));
                break;
            case PipelineConstants.MLMACD:
                MainAction.goals.add(new ServiceAction(market, ServiceAction.Task.MLMACD));
                break;
            case PipelineConstants.MLINDICATOR:
                MainAction.goals.add(new ServiceAction(market, ServiceAction.Task.MLINDICATOR));
                break;
            }
        }
        if (!toCheck.isEmpty()) {
            MainAction.goals.add(new FindProfitAction());
        }
    }

}
