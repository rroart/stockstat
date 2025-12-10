package roart.controller;

import java.util.function.Function;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.config.IclijConfig;
import roart.model.io.IO;

public class TestCommunicationFactory extends CommunicationFactory {
    private IO io;
    private IclijConfig conf;

    @Override
    public Communication get(String name, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage, WebFluxUtil webFluxUtil) {
        Communication communication = new TestCommunication(name, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);
        ((TestCommunication)communication).setIo(io);
        ((TestCommunication)communication).setConfig(conf);
        return communication;
    }

    public void setIo(IO io) {
        this.io = io;
    }

    public void setConfig(IclijConfig conf) {
        this.conf = conf;
    }
}
