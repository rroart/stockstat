package roart.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.util.MetaUtil;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ActionComponentItem;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.util.MarketUtil;

public class ActionThread extends Thread {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static volatile List<ActionComponentItem> queue = Collections.synchronizedList(new ArrayList<>());

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
        }
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        while (true) {
            ActionComponentItem ac = null;
            List<ActionComponentItem> list = new ArrayList<>();
            try {
                 list = ac.getAll();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<ActionComponentItem> copy = new ArrayList<>(queue);
            queue.removeAll(copy);
            list.addAll(copy);
            Comparator<ActionComponentItem> comparator = (ActionComponentItem i1, ActionComponentItem i2) -> getScore(i1) - getScore(i2);
            Collections.sort(list, comparator);
            if (!list.isEmpty()) {
                ActionComponentItem item = list.get(0);
                if (item.getDbid() == null) {
                    copy.remove(0);
                    queue.addAll(copy);
                }
                if (!MarketAction.enoughTime(instance, item)) {
                    if (item.getDbid() == null) {
                        queue.add(item);
                    }
                    continue;
                }
                IclijConfig config = new IclijConfig(instance);
                config.setMarket(item.getMarket());
                MarketAction action = ActionFactory.get(item.getAction());
                action.setParent(action);
                Market market = new MarketUtil().findMarket(item.getMarket());
                //ComponentInput input = new ComponentInput(new IclijConfig(IclijXMLConfig.getConfigInstance()), null, null, null, null, true, false, new ArrayList<>(), new HashMap<>());
                ComponentInput input = new ComponentInput(config, null, item.getMarket(), null, null, true, false, new ArrayList<>(), new HashMap<>());
                ComponentData param = null;
                try {
                    param = ComponentData.getParam(input, 0, market);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                param.setAction(action.getName());
                List<String> stockDates = param.getService().getDates(item.getMarket());
                action.getParamDates(market, param, stockDates);
                List<MetaItem> metas = param.getService().getMetas();
                MetaItem meta = new MetaUtil().findMeta(metas, item.getMarket());
                boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
                Component component = action.getComponentFactory().factory(item.getComponent());
                boolean evolve = action.getEvolve(component, param);
                WebData myData = action.getWebData();
                action.getPicksFiltered(myData, param, config, item, evolve, wantThree);                

                try {
                    if (item.getDbid() != null) {
                        item.delete();
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    }

    private int getScore(ActionComponentItem i) {
        int run = i.isHaverun() ? 1 : 0;
        return (int) (100000 * (i.getPriority() + run) + i.getTime());
    }
}
