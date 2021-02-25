package roart.common.communication.message.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.message.model.MessageCommunication;
import org.apache.commons.codec.digest.DigestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kafka extends MessageCommunication {
    //public static final int MSGSIZE = 5242880;
    public void method() {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.122.219:9092");
        AdminClient admin = AdminClient.create(config);
        //creating new topic
        System.out.println("-- creating --");
        NewTopic newTopic = new NewTopic("my-new-topic", 1, (short) 1);
        admin.createTopics(Collections.singleton(newTopic));
        System.out.println("-- listing --");
        try {
            admin.listTopics().names().get().forEach(System.out::println);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }    }
    
    //String brokerAddr;
    //String topic;
    
    Producer<String, String> producer;
    KafkaConsumer<String, String> consumer;
    
    public Kafka(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection);
        if (send) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, connection); // List of brokers that the producer asks to get the topic leader
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.RETRIES_CONFIG, 0);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
            
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
               StringSerializer.class.getName());
               
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
                    StringSerializer.class.getName());
            
            //props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, MSGSIZE);
            producer = new KafkaProducer<>(props);

        }
        if (receive) {
            Properties props = new Properties();
            
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, connection);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                    StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
                    StringDeserializer.class.getName());
            //props.put("fetch.message.max.bytes", MSGSIZE);
            //props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, MSGSIZE);
            consumer = new KafkaConsumer<>(props);

            List<TopicPartition> partitions = new ArrayList<>(); 
            List<PartitionInfo> partitionInfos = null;
            partitionInfos = consumer.partitionsFor(getReceiveService());
            if (partitionInfos != null) {
                for (PartitionInfo partition : partitionInfos)
                    partitions.add(new TopicPartition(partition.topic(),
                        partition.partition()));
            }
            consumer.assign(partitions);

        }
    }
    
    public void send(String s) {
        /*
        if (s.length() > MSGSIZE) {
            log.error("Too big for Kafka");
        }
        */
        String md5Hex = DigestUtils.md5Hex(s).toUpperCase();      
        producer.send(new ProducerRecord<>(getSendService(), md5Hex, s));
        System.out.println("Message sent successfully");
        producer.close();
    }
    
    public String[] receiveString() {
        //Kafka Consumer subscribes list of topics here.
        //consumer.subscribe(Arrays.asList(topicName));
        //consumer.subscribe(Pattern.compile(topicName));
        Duration duration = Duration.ofSeconds(1);

        //print the topic name
        System.out.println("Subscribed to topic " + getReceiveService());
        String[] retRecord = null;
        int returned = 0;
        while (returned == 0) {
            ConsumerRecords<String, String> records = consumer.poll(duration);
            returned = records.count();
            retRecord = new String[returned];
            int count = 0;
            for (ConsumerRecord<String, String> record : records) {
                retRecord[count++] = record.value();
                System.out.printf("offset = %d, key = %s, value = %s\n", 
                        record.offset(), record.key(), record.value());
            }
        }
        // print the offset,key and value for the consumer records.     
        //consumer.close();
        return retRecord;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void destroyTmp() {
        // TODO Auto-generated method stub
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.122.219:9092");
        AdminClient admin = AdminClient.create(config);
        List<String> list = new ArrayList<>();
        list.add(getReceiveService());
        DeleteTopicsResult deleteTopicsResult = admin.deleteTopics(list);

    }
}
