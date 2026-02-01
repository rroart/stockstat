package roart;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.message.kafka.Kafka;

import java.util.Date;
import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

public class Kafka2IT {

    String BROKER_ADDR = "192.168.122.219:9092";
    //@Test
    public void s() throws Exception{
        
        //Assign topicName to string variable
        String topicName = "my-new-topic";
        
        // create instance for properties to access producer configs   
        Properties props = new Properties();
        
        //Assign localhost id
        props.put("bootstrap.servers", BROKER_ADDR);
        
        //Set acknowledgements for producer requests.      
        props.put("acks", "all");
        
        //If the request fails, the producer can automatically retry,
        props.put("retries", 0);
        
        //Specify buffer size in config
        props.put("batch.size", 16384);
        
        //Reduce the no of requests less than 0   
        props.put("linger.ms", 1);
        
        //The buffer.memory controls the total amount of memory available to the producer for buffering.   
        props.put("buffer.memory", 33554432);
        
        props.put("key.serializer", 
           "org.apache.kafka.common.serialization.StringSerializer");
           
        props.put("value.serializer", 
           "org.apache.kafka.common.serialization.StringSerializer");
        
        Producer<String, String> producer = new KafkaProducer
           <String, String>(props);
              
        for(int i = 0; i < 10; i++)
           producer.send(new ProducerRecord<String, String>(topicName, 
              Integer.toString(i), Integer.toString(i)));
                 System.out.println("Message sent successfully");
                 producer.close();
     }    
    
    //@Test
    public void t() throws Exception {
        //Kafka consumer configuration settings
        String topicName = "my-new-topic";
        Properties props = new Properties();
        
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", 
           "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", 
           "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer
           <String, String>(props);
        
        //Kafka Consumer subscribes list of topics here.
        consumer.subscribe(Arrays.asList(topicName));
        
        //print the topic name
        System.out.println("Subscribed to topic " + topicName);
        int i = 0;
        
        while (true) {
           ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
           for (ConsumerRecord<String, String> record : records)
           
           // print the offset,key and value for the consumer records.
           System.out.printf("offset = %d, key = %s, value = %s\n", 
              record.offset(), record.key(), record.value());
        }
     }
    //@Test 
    public void v() {
        //new Kafka2(BROKER_ADDR, "mytopic").method();
    }
    
    Kafka k2;
    
    @BeforeEach
    public void b() {
        k2 = new Kafka("KAFKA", String.class, "tasks", new ObjectMapper(), true, true, false, BROKER_ADDR, null);
    }
    @Test
    public void v1() {
        k2.send("s");
    }
    
    @Test
    public void v2() {
        String[] s = k2.receiveString();
        System.out.println(s);
    }
    
}
