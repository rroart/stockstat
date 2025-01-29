package roart.action;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roart.common.util.JsonUtil;
import roart.common.model.ActionComponentItem;
import roart.common.model.TimingBLItem;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.Parameters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import roart.controller.IclijController;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest(classes = IclijController.class)
public class ActionThreadTest {

    @Autowired
    public IclijDbDao dbDao;

    @Autowired
    public IclijConfig iclijConfig;
    
    @Test
    public void test() {
        //IclijDbDao dbDao = mock(IclijDbDao.class);
        ActionThread t = new ActionThread(iclijConfig, dbDao);

        ActionComponentItem item1 = new ActionComponentItem();
        item1.setPriority(-20);
        item1.setHaverun(false);
        ActionComponentItem item2 = new ActionComponentItem();
        item2.setPriority(50);
        item2.setTime(98);
        item2.setHaverun(true);   	
        ActionComponentItem item3 = new ActionComponentItem();
        item3.setPriority(50);
        item3.setTime(100.5);
        item3.setHaverun(true);   	
        ActionComponentItem item4 = new ActionComponentItem();
        item4.setPriority(50);
        item4.setTime(100.6);
        item4.setHaverun(true);   	
        ActionComponentItem item5 = new ActionComponentItem();
        item5.setPriority(50);
        item5.setTime(102);
        item5.setHaverun(true);   	
        ActionComponentItem item6 = new ActionComponentItem();
        item6.setPriority(50);

        assertTrue(t.getScore(item1) < t.getScore(item6));
        assertTrue(t.getScore(item6) < t.getScore(item5));
        assertTrue(t.getScore(item5) < t.getScore(item4));
        assertTrue(t.getScore(item4) == t.getScore(item3));
        assertTrue(t.getScore(item3) < t.getScore(item2));
    }

    @Test
    public void test2() {
        System.out.println("Run");
        Parameters parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);
        //Map m = new HashMap<>();
        //m.put("futuredays", 10);
        //m.put("threshold", 1.0);
        System.out.println("" + JsonUtil.convert(parameters));
        //if (true) return;
        
        //TimingBLItem item = new TimingBLItem();
        ActionComponentItem item = new ActionComponentItem();
        item.setMarket("omxs");
        item.setAction("crosstest");
        item.setComponent("mlatr");
        item.setSubcomponent("tensorflow gru");
        item.setParameters(JsonUtil.convert(parameters));
        item.setBuy(null);
        item.setPriority(40);
        item.setRecord(LocalDate.now());
        ActionThread.queue.addAll(List.of(item));

        //IclijDbDao dbDao = mock(IclijDbDao.class);
        ActionThread thread = mock(ActionThread.class);
        //ActionFactory actionFactory = mock(ActionFactory.class);
        //MarketAction action = mock(MarketAction.class);
        
        //List<ActionComponentItem> spyList = spy(new ArrayList<>());        
        //spyList.add(item);
        List<ActionComponentItem> list = new ArrayList<>();        
        list.add(item);
        List<TimingBLItem> list2 = new ArrayList<>();        
       
        //when(dbDao.getAllActionComponent()).thenReturn(list);
        //when(dbDao.getAllTimingBLItem()).thenReturn(list2);
        //when(actionFactory.get(any(), any())).thenReturn(action);
        //when(action.getName()).thenReturn("");
        //doThrow(IllegalArgumentException.class)
        //.when(action)
        //.getPicksFilteredOuter(any(), any(), any(), any(), any(), any(), any());                
        doThrow(IllegalArgumentException.class)
        .when(thread)
        .runAction(any(), any(), any(), null);
        //when(thread.runAction(any(), any(), any()).thenReturn(true);
        ActionThread t = new ActionThread(iclijConfig, dbDao);
        System.out.println("Thr" +t );
        System.out.println("Thr" +thread );
        t.thread = thread;
        t.count = 0;
        System.out.println("Thr" +t.thread );
        t.run();

    }
}
