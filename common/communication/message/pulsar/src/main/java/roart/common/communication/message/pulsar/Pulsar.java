package roart.common.communication.message.pulsar;

import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.message.model.MessageCommunication;
import roart.common.constants.Constants;

import java.util.function.Function;

public class Pulsar extends MessageCommunication {

    String subscriptionName = "r";

    PulsarClient client = null;
    //Producer<byte[]> producer;
    Consumer<byte[]> consumer;
    Producer<String> stringProducer = null;

    public Pulsar(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);
        try {
            client = PulsarClient.builder()
                    .serviceUrl(connection)
                    .build();
        } catch (PulsarClientException e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (send) {
            /*
            try {
                producer = client.newProducer()
                        .topic(getSendService())
                        .compressionType(CompressionType.LZ4)
                        .create();
            } catch (PulsarClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
             */
            try {
                stringProducer = client.newProducer(Schema.STRING)
                        .topic(getSendService())
                        .create();
            } catch (PulsarClientException e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        if (receive) {
            try {
                consumer = client.newConsumer()
                        .topic(getReceiveService())
                        .subscriptionType(SubscriptionType.Shared)
                        .subscriptionName(subscriptionName)
                        .subscribe();
                //consumer.close();
            } catch (PulsarClientException e) {
                log.error(Constants.EXCEPTION, e);
            }
        }

    }

    public void send(String string) {

        /*
        String content = string;
        try {
            MessageId msgId = producer.send(content.getBytes());
        } catch (PulsarClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         */
        try {
            stringProducer.send(string);
        } catch (PulsarClientException e) {
            log.error(Constants.EXCEPTION, e);
        }        

        /*
        Message<byte[]> msg = ByteBufMessageBuilder
                .setContent(string.getBytes())
                .build();
*/
        try {
            stringProducer.close();
        } catch (PulsarClientException e) {
            log.error(Constants.EXCEPTION, e);
        }
        /*
        try {
            client.close();
        } catch (PulsarClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         */
    }

    @Override
    public String[] receiveString() {
        String string = null;
        // Wait for a message

        try {
            Message msg = consumer.receive();

            try {
                // Do something with the message
                string = new String(msg.getData());

                // Acknowledge the message so that it can be deleted by the message broker
                consumer.acknowledge(msg);
            } catch (Exception e) {
                // Message failed to process, redeliver later
                consumer.negativeAcknowledge(msg);
                return new String[0];
            }
        } catch (PulsarClientException e) {
            log.error(Constants.EXCEPTION, e);
            return new String[0];
        }
        /*
        try {
            client.close();
        } catch (PulsarClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
        return new String[] { string };

    }

    @Override
    public String[] receiveStringAndStore() {
        String string = null;
        Message msg = null;
        
        try {
            msg = consumer.receive(5, java.util.concurrent.TimeUnit.SECONDS);
            
            if (msg == null) {
                return new String[0];
            }

            try {
                // Do something with the message
                string = new String(msg.getData());
                
                boolean stored = false;
                try {
                    stored = storeMessage.apply(string);
                } catch (Exception e) {
                    log.error("Error storing message, will redeliver: {}", e.getMessage(), e);
                    // If storage fails, redeliver the message
                    consumer.negativeAcknowledge(msg);
                    return new String[0];
                }

                // Acknowledge the message so that it can be deleted by the message broker
                if (stored) {
                    consumer.acknowledge(msg);
                    return new String[] { string };
                } else {
                    consumer.negativeAcknowledge(msg);
                    return new String[0];
                }
            } catch (Exception e) {
                // Message failed to process, ensure redeliver
                log.error("Error processing message, redelivering: {}", e.getMessage(), e);
                try {
                    consumer.negativeAcknowledge(msg);
                } catch (Exception ackError) {
                    log.error("Failed to send negative ack: {}", ackError.getMessage(), ackError);
                }
                return new String[0];
            }
        } catch (PulsarClientException e) {
            log.error("Pulsar receive error: {}", Constants.EXCEPTION, e);
            return new String[0];
        }

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        try {
            client.close();
        } catch (PulsarClientException e) {
            log.error(Constants.EXCEPTION, e);
        }

    }
    
    @Override
    public void destroyTmp() {
        // TODO Auto-generated method stub
    }
}
