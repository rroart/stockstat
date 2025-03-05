package roart.action;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.beans.factory.annotation.Autowired;

import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.leader.MyLeader;
import roart.common.leader.impl.MyLeaderFactory;
import roart.common.model.AboveBelowItem;
import roart.common.model.ActionComponentItem;
import roart.common.model.IncDecItem;
import roart.common.model.MemoryItem;
import roart.common.model.MetaItem;
import roart.common.model.TimingBLItem;
import roart.common.model.TimingItem;
import roart.common.model.util.MetaUtil;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.iclij.component.Component;
import roart.iclij.component.factory.ComponentFactory;
import roart.component.model.ComponentData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;
import roart.iclij.service.util.MarketUtil;
import roart.populate.PopulateThread;
import roart.constants.IclijConstants;
import roart.controller.IclijController;
import roart.model.io.IO;

public class ActionThread extends Thread {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static volatile List<ActionComponentItem> queue = Collections.synchronizedList(new ArrayList<>());

    public static volatile Set<String> queued = Collections.synchronizedSet(new HashSet<>());

    private static volatile boolean updateDb = false;

    private static volatile boolean pause = false;

    public ActionThread thread;

    public Integer count;

    private IclijConfig iclijConfig;

    private IO io;
    
    public ActionThread(IclijConfig iclijConfig, IO io) {
        this.iclijConfig = iclijConfig;
        this.io = io;
        this.thread = this;
    }

    public static boolean isUpdateDb() {
        return updateDb;
    }

    public static void setUpdateDb(boolean updateDb) {
        ActionThread.updateDb = updateDb;
    }

    public static boolean isPause() {
        return pause;
    }

    public static void setPause(boolean pause) {
        ActionThread.pause = pause;
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
            Thread.currentThread().interrupt();
        }
        List<TimingBLItem> blacklist = null;
        try {
            blacklist = io.getIdbDao().getAllTimingBLItem();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        // if leader
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        long lastMain = 0;
        MyLeader leader = new MyLeaderFactory().create("action",  hostname, iclijConfig, io.getCuratorClient(), null /*GetHazelcastInstance.instance(conf.getInmemoryHazelcast())*/);

        while (true) {
            if (count != null && count-- < 0) {
                return;
            }
            if (updateDb || pause) {
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException e) {
                    log.error(Constants.EXCEPTION, e);
                    Thread.currentThread().interrupt();
                }
                continue;
            }
            boolean leading = leader.await(1, TimeUnit.SECONDS);
            if (!LeaderRunner.commonleader) {
                log.info("I am not action leader");
            } else {
            log.info("I am action leader");
            ActionComponentItem ac = null;
            List<ActionComponentItem> dblist = new ArrayList<>();
            try {
                dblist = io.getIdbDao().getAllActionComponent();
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
                if (!MarketAction.enoughTime(iclijConfig, item)) {
                    if (item.getDbid() == null) {
                        //queue.add(item);
                    }
                    continue;
                }
                String id = item.toStringId();
                log.info("Working with id {}", id);
                TimingBLItem blItem = blacklist.stream().filter(anitem -> id.equals(anitem.getId())).findAny().orElse(null);
                List<TimingBLItem> checkList = blacklist.stream().filter(anitem -> id.equals(anitem.getId())).toList();
                if (checkList.size() > 1) {
                    log.error("List size {} for {}", checkList.size(), id);
                }
                if (blItem != null && blItem.getCount() >= 3) {
                    continue;
                } 
                if (blItem == null) {
                    blItem = new TimingBLItem();
                    blItem.setId(id);
                    blItem.setRecord(LocalDate.now());
                } else {
                    try {
                        io.getIdbDao().deleteById(blItem, id);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }                	
                }
                blItem.setCount(1 + blItem.getCount());
                try {
                    io.getIdbDao().save(blItem);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                boolean finished = false;
                try {
                    finished = thread.runAction(iclijConfig, item, dblist);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                try {
                    io.getIdbDao().deleteById(blItem, id);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                try {
                    // TODO don't delete if failed
                    if (item.getDbid() != null) {
                        io.getIdbDao().deleteById(item, "" + item.getDbid());
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                if (!finished && item.getDbid() != null) {
                    try {
                        item.setPriority(100 + item.getPriority());
                        io.getIdbDao().save(item);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }
            }
            log.info("Leader status action: {}", leader.isLeader());            
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean runAction(IclijConfig instance, ActionComponentItem item, List<ActionComponentItem> dblist) {
        boolean finished = false;
        IclijConfig myConfig = new IclijConfig(iclijConfig);
        myConfig.getConfigData().setMarket(item.getMarket());
        MarketAction action = ActionFactory.get(item.getAction(), io.getIdbDao(), myConfig);
        action.setParent(action);
        Market market = new MarketUtil().findMarket(item.getMarket(), myConfig);
        //ComponentInput input = new ComponentInput(new IclijConfig(IclijXMLConfig.getConfigInstance()), null, null, null, null, true, false, new ArrayList<>(), new HashMap<>());
        ComponentInput input = new ComponentInput(myConfig.getConfigData(), null, item.getMarket(), null, null, true, false, new ArrayList<>(), new HashMap<>());
        ComponentData param = null;
        try {
            param = ComponentData.getParam(myConfig, input, 0, market, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        param.setAction(action.getName());
        // TODO null param id_
        List<String> stockDates = param.getService().getDates(item.getMarket(), param.getId());
        if (!IclijConfigConstants.DATASET.equals(action.getName())) {
            // TODO null param id_
            action.getParamDates(market, param, stockDates);
        }
        List<MetaItem> metas = param.getService().getMetas();
        MetaItem meta = new MetaUtil().findMeta(metas, item.getMarket());
        boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
        Component component = new ComponentFactory().factory(item.getComponent());
        boolean evolve = action.getEvolve(component, param);
        WebData myData = action.getWebData();
        if (item.getDbid() == null || action.getActionData().wantsUpdate(myConfig)) {
            String actionItem = LocalTime.now() + " " + Thread.currentThread().getId() + " " + item.toStringId();
            try {
                long[] mem0 = MemUtil.mem();
                log.info("Action item {} {}", item.toStringId(), MemUtil.print(mem0));
                action.getPicksFilteredOuter(myData, param, myConfig, item, evolve, wantThree, actionItem);                
                Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
                new PipelineThreadUtils(iclijConfig, inmemory, io.getCuratorClient()).cleanPipeline(param.getService().id, param.getId());
                //IclijController.taskList.remove(actionItem);
                long[] mem1 = MemUtil.mem();
                long[] memdiff = MemUtil.diff(mem1, mem0);
                log.info("Action mem {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
                finished = true;
                if (item.getDbid() != null) {
                    // flow
                    // better evolve triggers config save for ml, and new generated ml action
                    // better improveprofit triggers config save for findprofit, and new generated ml action (-db deletes and repopulation)
                    // if generated ml or improveprofit action
                    //    if generated ml action, then generate improveprofit action if none already
                    //	  db delete and repopulate
                    if (action.getActionData().wantsUpdate(myConfig)) {
                        if (IclijConstants.MACHINELEARNING.equals(item.getAction()) || IclijConstants.IMPROVEPROFIT.equals(item.getAction())) {
                            if (IclijConstants.MACHINELEARNING.equals(item.getAction())) {
                                //                            	otherAction = IclijConstants.IMPROVEPROFIT;
                                //                        	} else {
                                //                        		otherAction = IclijConstants.MACHINELEARNING;
                                MarketAction anAction = ActionFactory.get(IclijConstants.IMPROVEPROFIT, io.getIdbDao(), myConfig);
                                String mypriorityKey = anAction.getActionData().getPriority();
                                int aPriority = action.getPriority(myConfig, mypriorityKey);
                                ActionComponentItem it = dblist.stream().filter(dbitem -> (IclijConstants.IMPROVEPROFIT.equals(dbitem.getAction()) && item.getMarket().equals(dbitem.getMarket()) && item.getComponent().equals(dbitem.getComponent()) && item.getSubcomponent().equals(item.getSubcomponent()))).findAny().orElse(null); 
                                if (it == null && item.getPriority() == -10) {
                                    mct(IclijConstants.IMPROVEPROFIT, item.getMarket(), item.getComponent(), item.getSubcomponent(), aPriority);
                                }
                            }
                            //mct(item.getMarket(), IclijConstants.MACHINELEARNING, item.getComponent(), item.getSubcomponent());
                            // delete timing findprofit improveprofit
                            // only after improveprofit?
                            try {
                                log.info("Deleting AboveBelow etc {} {} {}", item.getMarket(), item.getComponent(), item.getSubcomponent());
                                io.getIdbDao().delete(new TimingItem(), item.getMarket(), IclijConstants.FINDPROFIT, item.getComponent(), item.getSubcomponent(), null, null);
                                io.getIdbDao().delete(new IncDecItem(), item.getMarket(), null, item.getComponent(), item.getSubcomponent(), null, null);
                                io.getIdbDao().delete(new MemoryItem(), item.getMarket(), null, item.getComponent(), item.getSubcomponent(), null, null);
                                io.getIdbDao().delete(new AboveBelowItem(), item.getMarket(), null, null, null, null, null);
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
        if (item.getDbid() != null && !action.getActionData().wantsUpdate(myConfig)) {
            finished = true;
        }
        return finished;
    }

    int getScore(ActionComponentItem i) {
        int run = i.isHaverun() ? 1 : 0;
        return (int) (100000 * (i.getPriority() + run) - i.getTime());
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
            io.getIdbDao().save(mct);
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
