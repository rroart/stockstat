package roart.common.communication.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// gemini

@Disabled
class CamelRabbitMQEndToEndTest {

    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;

    // Note: For this test to run successfully, a RabbitMQ instance must be accessible
    // at the default host (localhost) and port (5672).
    // The 'autoDelete=true' ensures the queue is cleaned up after the test.
    private static final String RABBITMQ_URI = "rabbitmq:myExchange?queue=myQueue&autoDelete=true";
    private static final String TEST_MESSAGE = "Hello RabbitMQ!";
    private static final String MOCK_RESULT_URI = "mock:result";

    @BeforeEach
    void setUp() throws Exception {
        camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                // Route to send messages to RabbitMQ
                from("direct:start")
                    .to(RABBITMQ_URI);

                // Route to consume messages from RabbitMQ and send to a mock endpoint for verification
                from(RABBITMQ_URI)
                    .to(MOCK_RESULT_URI);
            }
        });
        camelContext.start();
        producerTemplate = camelContext.createProducerTemplate();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (camelContext != null) {
            camelContext.stop();
        }
    }

    @Test
    void testSendAndReceiveStringFromRabbitMQ() throws Exception {
        // Get the mock endpoint to set expectations
        MockEndpoint mockEndpoint = camelContext.getEndpoint(MOCK_RESULT_URI, MockEndpoint.class);
        mockEndpoint.expectedBodiesReceived(TEST_MESSAGE);

        // Send the test message to the direct route
        Exchange sentExchange = producerTemplate.send("direct:start", exchange -> {
            exchange.getIn().setBody(TEST_MESSAGE);
        });

        // Assert that the message was sent without exceptions
        assertNotNull(sentExchange, "Sent Exchange should not be null");

        // Assert that the mock endpoint received the message
        mockEndpoint.assertIsSatisfied();

        System.out.println("Test message sent to and received from RabbitMQ: " + TEST_MESSAGE);
    }
}
