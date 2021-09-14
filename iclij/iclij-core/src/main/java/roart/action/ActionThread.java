package roart.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import roart.iclij.model.TimingBLItem;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ActionComponentItem;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.util.MarketUtil;

public class ActionThread extends Thread {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static volatile List<ActionComponentItem> queue = Collections.synchronizedList(new ArrayList<>());
    
    public static volatile Set<String> queued = Collections.synchronizedSet(new HashSet<>());
    
    private static volatile boolean updateDb = false;
    
    public static boolean isUpdateDb() {
        return updateDb;
    }

    public static void setUpdateDb(boolean updateDb) {
        ActionThread.updateDb = updateDb;
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
            Thread.currentThread().interrupt();
        }
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<TimingBLItem> blacklist = null;
        try {
            blacklist = TimingBLItem.getAll();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        while (true) {
            if (updateDb) {
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException e) {
                    log.error(Constants.EXCEPTION, e);
                    Thread.currentThread().interrupt();
                }
                continue;
            }
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
                // ???
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
                String id = item.toStringId();
                TimingBLItem blItem = blacklist.stream().filter(anitem -> id.equals(anitem.getId())).findAny().orElse(null);
                if (blItem != null && blItem.getCount() >= 3) {
                    continue;
                } else {
                    blItem = new TimingBLItem();
                    blItem.setId(id);
                }
                blItem.setCount(1 + blItem.getCount());
                try {
                    blItem.save();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                runAction(instance, item);
                try {
                    blItem.delete(id);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
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
                Thread.currentThread().interrupt();
            }
        }
    }

    private WebData runAction(IclijConfig instance, ActionComponentItem item) {
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
        if (item.getDbid() == null || action.getActionData().wantsUpdate(config)) {
            try {
                action.getPicksFiltered(myData, param, config, item, evolve, wantThree);                
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return myData;
    }

    private int getScore(ActionComponentItem i) {
        int run = i.isHaverun() ? 1 : 0;
        return (int) (100000 * (i.getPriority() + run) + i.getTime());
    }
    
    /*
     * Needs its own thread
                if (item.getResult() != null) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    Thread t2 = new Thread(new Runnable() {
                        public void run() { 
                            WebData webData = runAction(instance, item);
                            item.getResult().add(webData);
                        }});
                    t2.start();      
                }
     */
}
