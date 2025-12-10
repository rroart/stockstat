package roart.common.communication.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Channel;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.springrabbit.SpringRabbitMQComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.integration.model.IntegrationCommunication;
import roart.common.util.JsonUtil;
import roart.common.constants.Constants;

import java.io.IOException;
import java.util.function.Function;

public class Camel extends IntegrationCommunication {

    CamelContext context;
    ProducerTemplate producer;
    ConsumerTemplate consumer;
    String vhost = "task";

    public Camel(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);

        context = new DefaultCamelContext();

        //Exchange ex; ex.
        
        // Define a unique name for your Camel component
        String componentName = "RMQ_CAMEL_CONSUMER";

        log.info("Components " + context.getComponentNames());

        // Create an instance of SpringRabbitMQComponent
        try (SpringRabbitMQComponent component = new SpringRabbitMQComponent()) {
            // Set the configured connection factory
            component.setConnectionFactory(connectionFactory());
            
            // Optionally, configure additional properties
            component.setTestConnectionOnStartup(true); // Test the connection on startup
            component.setAutoStartup(true); // Automatically start the component

            // Register the component with Camel context
            context.addComponent(componentName, component);
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
        log.info("Components " + context.getComponentNames());
        
        context.start();
        if (send) {
            Endpoint endpoint = context.getEndpoint(connection + "/" + vhost + getSendService() + "?routingKey=camel&arg.queue.autoDelete=true&connectionFactory=rmq");
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
        log.info("Components " + producer.getCamelContext().getComponentNames());
        producer.sendBody(s);        
    }

    @Override
    public String[] receiveString() {
        Endpoint endpoint = context.getEndpoint(connection + "/" + vhost + getReceiveService() + "?acknowledgeMode=AUTO&routingKey=camel&connectionFactory=rmq&queues=" + getReceiveService());
        Exchange receive = consumer.receive(endpoint);
        return new String[] { receive.getIn().getBody(String.class) };
    }

    @Override
    public String[] receiveStringAndStore() {
        Endpoint endpoint = context.getEndpoint(connection + "/" + vhost + getReceiveService() + "?acknowledgeMode=MANUAL&routingKey=camel&queues=" + getReceiveService());
        Exchange receive = consumer.receive(endpoint);
        String body = receive.getIn().getBody(String.class);
        boolean stored = storeMessage.apply(body);
        if (stored) {
            receive.getUnitOfWork().done(receive);
            return new String[] { body };
        }
        return new String[] { };
    }

    public void destroy() {
        context.stop();
    }

    //@Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
            new CachingConnectionFactory(connection);
        connectionFactory.setUsername("username");
        connectionFactory.setPassword("password");
        /*
        if (sendreceive) {
            AmqpAdmin admin = new RabbitAdmin(connectionFactory);
            admin.declareQueue(new Queue(getSendService()));
            admin.declareQueue(new Queue(getReceiveService()));
         } else {
            if (send) {
                AmqpAdmin admin = new RabbitAdmin(connectionFactory);
                admin.declareQueue(new Queue(getSendService()));
            }
            if (receive) {
                AmqpAdmin admin = new RabbitAdmin(connectionFactory);
                admin.declareQueue(new Queue(getReceiveService()));
            }
        }
        //connectionFactory.setUsername("guest");
        //connectionFactory.setPassword("guest");
        */
        return connectionFactory;
    }
    
}
