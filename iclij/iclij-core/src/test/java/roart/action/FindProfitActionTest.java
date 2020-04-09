package roart.action;

import org.junit.Test;

import roart.iclij.config.IclijXMLConfig;

public class FindProfitActionTest {
    
    @Test
    public void test() {
        IclijXMLConfig.getConfigInstance();
        new FindProfitAction().goal(null);
    }
    
}
