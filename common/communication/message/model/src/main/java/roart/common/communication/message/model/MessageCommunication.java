package roart.common.communication.message.model;

import roart.common.communication.model.Communication;

import tools.jackson.databind.ObjectMapper;
import java.util.function.Function;

public abstract class MessageCommunication extends Communication {

    public MessageCommunication(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);
    }

}
