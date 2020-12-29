package roart.common.communication.message.model;

import roart.common.communication.model.Communication;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class MessageCommunication extends Communication {

    public MessageCommunication(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection);
    }

}
