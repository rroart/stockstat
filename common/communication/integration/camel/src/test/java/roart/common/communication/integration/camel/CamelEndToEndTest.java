package roart.common.communication.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// gemini

@Disabled
public class CamelEndToEndTest {

    private Camel camelIntegration;
    private ObjectMapper mapper;
    private Function<String, Boolean> storeMessage;
    private String testService = "testService";
    private String connectionUri = "rabbitmq://localhost:5672"; // Using a mock or local RabbitMQ instance

    // Mock ConnectionFactory for testing purposes
    private ConnectionFactory mockConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
        storeMessage = s -> true; // Simple mock function that always "stores" the message

        // Initialize the Camel integration with mock components
        camelIntegration = new Camel(
                "testCamel",
                String.class,
                testService,
                mapper,
                true, // send
                true, // receive
                false, // sendreceive
                connectionUri,
                storeMessage
        ) {
            // Override connectionFactory to use the mock one
            @Override
            public ConnectionFactory connectionFactory() {
                return mockConnectionFactory();
            }
        };
    }

    @AfterEach
    void tearDown() {
        if (camelIntegration != null) {
            camelIntegration.destroy();
        }
    }

    @Test
    void testSendAndReceiveString() throws Exception {
        String testMessage = "Hello, Camel!";

        // Send the message
        camelIntegration.send(testMessage);

        // Receive the message
        // Note: In a real scenario, you might need to wait for the message to be routed
        // For a direct test, it might be quick enough or require a small delay.
        String[] receivedMessages = camelIntegration.receiveString();

        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
        assertEquals(testMessage, receivedMessages[0]);
    }

    @Test
    void testSendAndReceiveStringAndStore() throws Exception {
        String testMessage = "Hello, Camel with store!";

        // Send the message
        camelIntegration.send(testMessage);

        // Receive and store the message
        String[] receivedMessages = camelIntegration.receiveStringAndStore();

        assertNotNull(receivedMessages);
        assertEquals(1, receivedMessages.length);
        assertEquals(testMessage, receivedMessages[0]);
    }
}
