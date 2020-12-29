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

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.message.model.MessageCommunication;

public class Pulsar extends MessageCommunication {

    String subscriptionName = "r";

    PulsarClient client = null;
    //Producer<byte[]> producer;
    Consumer<byte[]> consumer;
    Producer<String> stringProducer = null;

    public Pulsar(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection);
        try {
            client = PulsarClient.builder()
                    .serviceUrl(connection)
                    .build();
        } catch (PulsarClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        

        /*
        Message<byte[]> msg = ByteBufMessageBuilder
                .setContent(string.getBytes())
                .build();
*/
        try {
            stringProducer.close();
        } catch (PulsarClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            }
        } catch (PulsarClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    public void destroy() {
        // TODO Auto-generated method stub
        try {
            client.close();
        } catch (PulsarClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    @Override
    public void destroyTmp() {
        // TODO Auto-generated method stub
    }
}
