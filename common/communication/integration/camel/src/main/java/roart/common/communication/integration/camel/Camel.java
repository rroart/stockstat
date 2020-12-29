package roart.common.communication.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.integration.model.IntegrationCommunication;
import roart.common.util.JsonUtil;

public class Camel extends IntegrationCommunication {

    CamelContext context;
    ProducerTemplate producer;
    ConsumerTemplate consumer;
    String vhost = "task";

    public Camel(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection);
        context = new DefaultCamelContext();
        context.start();
        if (send) {
            Endpoint endpoint = context.getEndpoint(connection + "/" + vhost + getSendService() + "?autoDelete=false&routingKey=camel&queue=" + getSendService());
            producer = context.createProducerTemplate();
            producer.setDefaultEndpoint(endpoint);
        }
        if (receive) {
            consumer = context.createConsumerTemplate();
        }
    }

    /*
    public Camel(String queue) {
        context = new DefaultCamelContext();
        AMQPComponent amqpComponent = AMQPComponent.amqpComponent("amqp://localhost:5672");
        context.addComponent("amqp", amqpComponent);

    }
    public Camel(String vhost, String queue) {
        context = new DefaultCamelContext();
        context.start();
        endpoint = context.getEndpoint("rabbitmq://localhost:5672/" + vhost + "?autoDelete=false&routingKey=camel&queue=" + queue);
        producer = context.createProducerTemplate();
        producer.setDefaultEndpoint(endpoint);
        consumer = context.createConsumerTemplate();
    }
     */

    public void send(String s) {
        producer.sendBody(s);        
    }

    public String[] receiveString() {
        Endpoint endpoint = context.getEndpoint(connection + "/" + vhost + getReceiveService() + "?autoDelete=false&routingKey=camel&queue=" + getReceiveService());
        Exchange receive = consumer.receive(endpoint);
        return new String[] { receive.getIn().getBody(String.class) };
    }

    public void destroy() {
        context.stop();
    }

}
