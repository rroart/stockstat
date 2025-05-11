package roart.controller;

import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.model.Communication;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.model.Inmemory;
import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceResult;
import roart.model.io.IO;
import roart.evolve.Evolve;
import roart.sim.Sim;

public class TestCommunication extends Communication {

    private String sent;
    
    private IO io;

    private IclijConfig conf;
    
    public TestCommunication(String myname, Class myclass, String service, ObjectMapper mapper, boolean send,
            boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);
    }

    @Override
    public void send(String s) {
        sent = s;
        
        QueueElement element = JsonUtil.convert(s, QueueElement.class);
        Inmemory inmemory = io.getInmemoryFactory().get("", "", "");
        String content = inmemory.read(element.getMessage());
        inmemory.delete(element.getMessage());
        
        log.info("Service {}", getService());
        
        if (getService().equals(ServiceConstants.EVOLVEFILTEREVOLVE)) {
            IclijServiceResult r = new IclijServiceResult();
            new Evolve(conf, io).handleEvolve((String) content);            
        }
        if (getService().equals(ServiceConstants.EVOLVEFILTERPROFIT)) {
            IclijServiceResult r = new IclijServiceResult();
            new Evolve(conf, io).handleProfit((String) content);            
        }
        if (getService().equals(ServiceConstants.EVOLVEFILTERFILTER)) {
            IclijServiceResult r = new IclijServiceResult();
            new Evolve(conf, io).handleFilter((String) content);            
       }
        if (getService().equals(ServiceConstants.EVOLVEFILTERABOVEBELOW)) {
            IclijServiceResult r = new IclijServiceResult();
            new Evolve(conf, io).handleAboveBelow((String) content);            
        }
        if (getService().equals(ServiceConstants.SIMFILTER)) {
            new Sim(conf, io).method((String) content, "sim", true);
        }
        if (getService().equals(ServiceConstants.SIMAUTO)) {
            new Sim(conf, io).method((String) content, "simauto", true);
        }
        if (getService().equals(ServiceConstants.SIMRUN)) {
            new Sim(conf, io).method3((String) content, "simrun", true);
        }
        log.error("No service {}", getService());
    }

    @Override
    public String[] receiveString() {
        return null;
    }

    @Override
    public String[] receiveStringAndStore() {
        return new String[] { sent };
    }

    @Override
    public void destroy() {
    }

    public void setIo(IO io) {
        this.io = io;
    }

    public void setConfig(IclijConfig conf) {
        this.conf = conf;
    }
}
