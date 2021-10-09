package roart.action;

import java.time.LocalDate;
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

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.util.JsonUtil;
import roart.common.util.MetaUtil;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.model.AboveBelowItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingBLItem;
import roart.iclij.model.TimingItem;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ActionComponentItem;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.util.MarketUtil;
import roart.populate.PopulateThread;
import roart.constants.IclijConstants;

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
            List<ActionComponentItem> dblist = new ArrayList<>();
            try {
                 dblist = ac.getAll();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<ActionComponentItem> list = new ArrayList<>();
            list.addAll(dblist);
            List<ActionComponentItem> copy = new ArrayList<>(queue);
            queue.removeAll(copy);
            list.addAll(copy);
            Comparator<ActionComponentItem> comparator = (ActionComponentItem i1, ActionComponentItem i2) -> getScore(i1) - getScore(i2);
            Collections.sort(list, comparator);
            Collections.sort(copy, comparator);
            if (!list.isEmpty()) {
                ActionComponentItem item = list.get(0);
                // ???
                if (item.getDbid() == null) {
                    copy.remove(0);
                }
                queue.addAll(copy);
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
                } 
                if (blItem == null) {
                    blItem = new TimingBLItem();
                    blItem.setId(id);
                } else {
                    try {
                        blItem.delete(id);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }                	
                }
                blItem.setCount(1 + blItem.getCount());
                try {
                    blItem.save();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                runAction(instance, item, dblist);
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

    private WebData runAction(IclijConfig instance, ActionComponentItem item, List<ActionComponentItem> dblist) {
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
                if (item.getDbid() != null) {
                    if (action.getActionData().wantsUpdate(config)) {
                        if (IclijConstants.MACHINELEARNING.equals(item.getAction()) || IclijConstants.IMPROVEPROFIT.equals(item.getAction())) {
                            if (IclijConstants.MACHINELEARNING.equals(item.getAction())) {
                                MarketAction anAction = ActionFactory.get(IclijConstants.IMPROVEPROFIT);
                                String mypriorityKey = anAction.getActionData().getPriority();
                                int aPriority = action.getPriority(config, mypriorityKey);
                                ActionComponentItem it = dblist.stream().filter(dbitem -> (IclijConstants.IMPROVEPROFIT.equals(dbitem.getAction()) && item.getMarket().equals(dbitem.getMarket()) && item.getComponent().equals(dbitem.getComponent()) && item.getSubcomponent().equals(item.getSubcomponent()))).findAny().orElse(null); 
                                if (it == null) {
                                	mct(IclijConstants.IMPROVEPROFIT, item.getMarket(), item.getComponent(), item.getSubcomponent(), aPriority);
                                }
                            }
                            //mct(item.getMarket(), IclijConstants.MACHINELEARNING, item.getComponent(), item.getSubcomponent());
                            // delete timing findprofit improveprofit
			    // only after improveprofit?
                            try {
                            	log.info("Deleting AboveBelow etc {} {} {}", item.getMarket(), item.getComponent(), item.getSubcomponent());
                                new TimingItem().delete(item.getMarket(), IclijConstants.FINDPROFIT, item.getComponent(), item.getSubcomponent(), null, null);
                                new IncDecItem().delete(item.getMarket(), item.getComponent(), item.getSubcomponent(), null, null);
                                new MemoryItem().delete(item.getMarket(), item.getComponent(), item.getSubcomponent(), null, null);
                                new AboveBelowItem().delete(item.getMarket(), null, null);
                            } catch (Exception e) {
                                log.error(Constants.EXCEPTION, e);
                            }                        
                            PopulateThread.queue.add(new ImmutableTriple(item.getMarket(), item.getComponent(), item.getSubcomponent()));
                        }
                    }
                    
                }
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
    
    private void mct(String action, String market, String component, String subcomponent, int priority) {
        Parameters p = new Parameters();
        p.setFuturedays(10);
        p.setThreshold(1.0);
        ActionComponentItem mct = new ActionComponentItem();
        mct.setAction(action);
        mct.setMarket(market);
        mct.setComponent(component);
        mct.setSubcomponent(subcomponent);
        mct.setRecord(LocalDate.now());
        mct.setPriority(priority);
        mct.setParameters(JsonUtil.convert(p));
        try {
            mct.save();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
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
