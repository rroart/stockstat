package roart.action;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.ArrayList;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import roart.common.util.JsonUtil;
import roart.common.model.ActionComponentDTO;
import roart.common.model.TimingBLDTO;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.bean.ConfigI;
import roart.iclij.model.Parameters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import roart.controller.ConfigDb;
import roart.controller.IclijController;
import roart.model.io.IO;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest(classes = { IclijConfig.class, ConfigI.class, IclijDbDao.class, ConfigDb.class })
public class ActionThreadTest {

    @Autowired
    public IclijDbDao dbDao;

    @Autowired
    public IclijConfig iclijConfig;
    
    // TODO IT @Test
    public void test() {
        //IclijDbDao dbDao = mock(IclijDbDao.class);
        IO io = null;
        ActionThread t = new ActionThread(iclijConfig, io);

        ActionComponentDTO item1 = new ActionComponentDTO();
        item1.setPriority(-20);
        item1.setHaverun(false);
        ActionComponentDTO item2 = new ActionComponentDTO();
        item2.setPriority(50);
        item2.setTime(98);
        item2.setHaverun(true);   	
        ActionComponentDTO item3 = new ActionComponentDTO();
        item3.setPriority(50);
        item3.setTime(100.5);
        item3.setHaverun(true);   	
        ActionComponentDTO item4 = new ActionComponentDTO();
        item4.setPriority(50);
        item4.setTime(100.6);
        item4.setHaverun(true);   	
        ActionComponentDTO item5 = new ActionComponentDTO();
        item5.setPriority(50);
        item5.setTime(102);
        item5.setHaverun(true);   	
        ActionComponentDTO item6 = new ActionComponentDTO();
        item6.setPriority(50);

        assertTrue(t.getScore(item1) < t.getScore(item6));
        assertTrue(t.getScore(item6) < t.getScore(item5));
        assertTrue(t.getScore(item5) < t.getScore(item4));
        assertTrue(t.getScore(item4) == t.getScore(item3));
        assertTrue(t.getScore(item3) < t.getScore(item2));
    }

    // TODO IT @Test
    public void test2() {
        IO io = mock(IO.class);
        CuratorFramework curatorClient = mock(CuratorFramework.class);
        doReturn(curatorClient).when(io).getCuratorClient();

        System.out.println("Run");
        Parameters parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);
        //Map m = new HashMap<>();
        //m.put("futuredays", 10);
        //m.put("threshold", 1.0);
        System.out.println("" + JsonUtil.convert(parameters));
        //if (true) return;
        
        //TimingBLDTO item = new TimingBLDTO();
        ActionComponentDTO item = new ActionComponentDTO();
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
        
        //List<ActionComponentDTO> spyList = spy(new ArrayList<>());        
        //spyList.add(item);
        List<ActionComponentDTO> list = new ArrayList<>();        
        list.add(item);
        List<TimingBLDTO> list2 = new ArrayList<>();        
       
        //when(dbDao.getAllActionComponent()).thenReturn(list);
        //when(dbDao.getAllTimingBLDTO()).thenReturn(list2);
        //when(actionFactory.get(any(), any())).thenReturn(action);
        //when(action.getName()).thenReturn("");
        //doThrow(IllegalArgumentException.class)
        //.when(action)
        //.getPicksFilteredOuter(any(), any(), any(), any(), any(), any(), any());                
        doThrow(IllegalArgumentException.class)
        .when(thread)
        .runAction(any(), any(), any());
        //when(thread.runAction(any(), any(), any()).thenReturn(true);
        ActionThread t = new ActionThread(iclijConfig, io);
        System.out.println("Thr" +t );
        System.out.println("Thr" +thread );
        t.thread = thread;
        t.count = 0;
        System.out.println("Thr" +t.thread );
        try {
        t.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
