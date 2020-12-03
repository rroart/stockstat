package roart.common.communication.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import roart.common.communication.model.Communication;
import roart.common.communication.integration.camel.Camel;
import roart.common.communication.integration.spring.Spring;
import roart.common.communication.message.kafka.KafkaNot;
import roart.common.communication.message.kafka.Kafka;
import roart.common.communication.message.pulsar.Pulsar;
import roart.common.communication.rest.REST;

public class CommunicationFactory {
    public static Communication get(String name, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection) {
        switch (name) {
        case "REST":
            return new REST("REST", myclass, service, mapper, send, receive, sendreceive, connection);
        case "CAMEL":
            return new Camel("CAMEL", myclass, service, mapper, send, receive, sendreceive, connection);
        case "SPRING":
            return new Spring("SPRING", myclass, service, mapper, send, receive, sendreceive, connection);
        case "PULSAR":
            return new Pulsar("PULSAR", myclass, service, mapper, send, receive, sendreceive, connection);
        case "KAFKA":
            return new Kafka("KAFKA", myclass, service, mapper, send, receive, sendreceive, connection);
        }
        return null;
    }
}
