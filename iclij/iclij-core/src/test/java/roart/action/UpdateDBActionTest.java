package roart.action;

import org.junit.Test;

import roart.iclij.config.IclijXMLConfig;

public class UpdateDBActionTest {
    
    @Test
    public void test() throws InterruptedException {
        IclijXMLConfig.getConfigInstance();
        new UpdateDBAction().goal(null);
    }
}
