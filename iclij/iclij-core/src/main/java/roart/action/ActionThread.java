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
import roart.iclij.model.WebData;
import roart.iclij.model.action.ActionComponentItem;
import roart.iclij.model.component.ComponentInput;

public class ActionThread extends Thread {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static volatile Deque<ActionComponentItem> queue = new ConcurrentLinkedDeque<>();

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
        }
        IclijConfig config = IclijXMLConfig.getConfigInstance();
        while (true) {
            ActionComponentItem ac = null;
            List<ActionComponentItem> list = null;
            try {
                 list = ac.getAll();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
            Comparator<ActionComponentItem> comparator = (ActionComponentItem i1, ActionComponentItem i2) -> i1.getPriority() - i2.getPriority();
            Collections.sort(list, comparator);
            for (ActionComponentItem item : list) {
                MarketAction action = ActionFactory.get(item.getAction());
                ComponentInput input = new ComponentInput(new IclijConfig(IclijXMLConfig.getConfigInstance()), null, null, null, null, true, false, new ArrayList<>(), new HashMap<>());
                ComponentData param = null;
                try {
                    param = ComponentData.getParam(input, 0);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                List<MetaItem> metas = param.getService().getMetas();
                MetaItem meta = new MetaUtil().findMeta(metas, item.getMarket());
                boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
                boolean evolve = false;
                WebData myData = action.getWebData();
                action.getPicksFiltered(myData, param, config, item, evolve, wantThree);                

                try {
                    item.delete();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            List<ActionComponentItem> run = queue.stream().filter(m -> m.isHaverun()).collect(Collectors.toList());
            List<ActionComponentItem> notrun = queue.stream().filter(m -> !m.isHaverun()).collect(Collectors.toList());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    }
}
