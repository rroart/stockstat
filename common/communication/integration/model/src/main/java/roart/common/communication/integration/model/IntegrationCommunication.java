package roart.common.communication.integration.model;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.model.Communication;
import java.util.function.Function;

public abstract class IntegrationCommunication extends Communication {

    public IntegrationCommunication(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);
    }

}
