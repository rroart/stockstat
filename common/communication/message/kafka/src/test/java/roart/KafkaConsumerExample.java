package roart;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
 
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * Kafka console message consumer.
 */
public class KafkaConsumerExample {
    private final static String TOPIC_NAME = KafkaProducerExample.TOPIC_NAME;
    public final static Logger logger = LoggerFactory.getLogger(KafkaConsumerExample.class.getName());
 
    @Test
    public void mainm() {
        // consume messages
        Consumer<String, String> consumer = KafkaConsumerExample.createConsumer();
 
        // subscribe to the test topic
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));
        try {
            String receivedText = null;
            while (!"exit".equalsIgnoreCase(receivedText)) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    receivedText = record.value();
                    if (receivedText != null) {
                        logger.info(
                                "Message received ==> topic = {}, partition = {}, offset = {}, key = {}, value = {}",
                                record.topic(), record.partition(), record.offset(), record.key(), receivedText);
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }
 
    private static Consumer<String, String> createConsumer() {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.122.219:9092");
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test_consumer_group");
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new KafkaConsumer<String, String>(kafkaProps);
    }
}