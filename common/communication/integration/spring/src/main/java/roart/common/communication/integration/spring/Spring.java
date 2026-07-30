package roart.common.communication.integration.spring;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import roart.common.communication.integration.model.IntegrationCommunication;
import roart.common.util.JsonUtil;
import roart.common.constants.Constants;

public class Spring extends IntegrationCommunication {

    private RabbitTemplate template = rabbitTemplate();
    
    //private Queue queue = new Queue("myq");

    public Spring(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);
      /*
        ConnectionFactory connectionFactory = new CachingConnectionFactory();
        AmqpAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(new Queue(queueName));
        template = new RabbitTemplate(connectionFactory);
        */
    }
    
    //Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
            new CachingConnectionFactory(connection);
        connectionFactory.setUsername("username");
        connectionFactory.setPassword("password");
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
        return connectionFactory;
    }
    
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        if (storeMessage == null ) {
            template.containerAckMode(AcknowledgeMode.AUTO);
            log.info("Ack auto");
        } else {
            template.containerAckMode(AcknowledgeMode.MANUAL);
            log.info("Ack manual");
        }
        //template.setRoutingKey(this.helloWorldQueueName);
        return template;
    }
    
    public void send(String string) {
        Runnable r = () -> {
        template.convertAndSend(getSendService(), string);
        try {
            Thread.sleep(10000);
        } catch (Exception e) {}
        };
        new Thread(r).start();
        //template.destroy();
        //((CachingConnectionFactory)connectionFactory()).destroy();
    }

    @Override
    public String[] receiveString() {
        String[] strings = new String[0];
        String string = null;
        while (true) {
            template.containerAckMode(null);
            Object object = template.receiveAndConvert(getReceiveService(), 1000);
            if (object == null) {
                continue;
                // TODO
                //break;
            }
            //System.out.println("boj" + Arrays.asList(object));
            if (object == null || object instanceof String) {
                string = (String) object;
            } else {
                string = new String((byte[]) object);
            }
            strings = ArrayUtils.addAll(strings, string);
            break;
        }
        template.destroy();
        ((CachingConnectionFactory)connectionFactory()).destroy();
        return strings;
    }
    
    @Override
    public String[] receiveStringAndStore() {
        String[] strings = new String[0];
        String string = null;
        Message message = null;
        Channel ch = null;
        
        try {
            template.containerAckMode(AcknowledgeMode.MANUAL);
            message = template.receive(getReceiveService(), 1000);
            
            if (message == null) {
                return new String[0];
            }
            
            Object object = message.getBody();
            if (object == null || object instanceof String) {
                string = (String) object;
            } else {
                string = new String((byte[]) object);
            }
            
            long tag = message.getMessageProperties().getDeliveryTag();
            
            try {
                ch = template.getConnectionFactory().createConnection().createChannel(false);
                
                boolean stored = false;
                try {
                    stored = storeMessage.apply(new String(string));
                } catch (Exception e) {
                    log.error("Error storing message, will redeliver: {}", e.getMessage(), e);
                    // If storage fails, nack with requeue
                    try {
                        ch.basicNack(tag, false, true);
                    } catch (IOException nackError) {
                        log.error("Failed to send nack: {}", nackError.getMessage(), nackError);
                    }
                    return new String[0];
                }
                
                if (stored) {
                    try {
                        ch.basicAck(tag, false);
                        strings = ArrayUtils.addAll(strings, string);
                    } catch (IOException ackError) {
                        log.error("Failed to send ack: {}", ackError.getMessage(), ackError);
                        // Try to nack if ack fails
                        try {
                            ch.basicNack(tag, false, true);
                        } catch (IOException nackError) {
                            log.error("Failed to send nack after ack error: {}", nackError.getMessage(), nackError);
                        }
                    }
                } else {
                    try {
                        ch.basicNack(tag, false, true);
                    } catch (IOException nackError) {
                        log.error("Failed to send nack: {}", nackError.getMessage(), nackError);
                    }
                }
            } catch (Exception e) {
                log.error("Error creating channel: {}", e.getMessage(), e);
                return new String[0];
            } finally {
                // Close channel safely
                if (ch != null && ch.isOpen()) {
                    try {
                        ch.close();
                    } catch (Exception closeError) {
                        log.debug("Error closing channel: {}", closeError.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in receiveStringAndStore: {}", e.getMessage(), e);
            return new String[0];
        } finally {
            // Clean up template and connection factory
            try {
                template.destroy();
            } catch (Exception e) {
                log.debug("Error destroying template: {}", e.getMessage());
            }
            try {
                ((CachingConnectionFactory)connectionFactory()).destroy();
            } catch (Exception e) {
                log.debug("Error destroying connection factory: {}", e.getMessage());
            }
        }
        
        return strings;
    }
    
    public void simpleRequest() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames("myq");
        container.setMessageListener(new MessageListenerAdapter(new HelloWorldHandler()));
        
        Random r = new Random();
        int account = r.nextInt((99999 - 1) + 1) + 1;
        String message = "Request balance for account " + account;
        System.out.println("" + template);
        //System.out.println("" + queue);
        //this.template.convertAndSend(queue.getName(), message);
        System.out.println(" [x] Sent '" + message + "'");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("end");
    }
    
    public class HelloWorldHandler {

        public void handleMessage(String text) {
            System.out.println("Received: " + text);
        }

    }
    
    @RabbitListener(queues = "myq")
    public class Balance {

        @RabbitHandler
        public void createBalance(String request) {
            System.out.println(" [x] Received '" + request + "'");
        }

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

    protected void destroyTmp() {
        if (sendreceive) {
            CachingConnectionFactory connectionFactory =
                    new CachingConnectionFactory(connection);
            AmqpAdmin admin = new RabbitAdmin(connectionFactory);
            admin.deleteQueue(getReceiveService());
        }
    }

}
