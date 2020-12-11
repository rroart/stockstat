package roart.common.communication.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import roart.common.communication.model.Communication;
import roart.common.communication.integration.camel.Camel;
import roart.common.communication.integration.spring.Spring;
import roart.common.communication.message.kafka.Kafka;
import roart.common.communication.message.pulsar.Pulsar;
import roart.common.communication.rest.REST;
import roart.common.constants.CommunicationConstants;

public class CommunicationFactory {
    public static Communication get(String name, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection) {
        switch (name) {
        case CommunicationConstants.REST:
            return new REST(name, myclass, service, mapper, send, receive, sendreceive, connection);
        case CommunicationConstants.CAMEL:
            return new Camel(name, myclass, service, mapper, send, receive, sendreceive, connection);
        case CommunicationConstants.SPRING:
            return new Spring(name, myclass, service, mapper, send, receive, sendreceive, connection);
        case CommunicationConstants.PULSAR:
            return new Pulsar(name, myclass, service, mapper, send, receive, sendreceive, connection);
        case CommunicationConstants.KAFKA:
            return new Kafka(name, myclass, service, mapper, send, receive, sendreceive, connection);
        }
        return null;
    }
}
