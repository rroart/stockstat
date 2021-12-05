package roart.action;

import org.junit.jupiter.api.Test;

import roart.iclij.model.action.ActionComponentItem;

import static org.junit.jupiter.api.Assertions.*;

public class ActionThreadTest {

	@Test
	public void test() {
		ActionThread t = new ActionThread();
		
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
}
