package roart.common.communication.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// gemini

class CamelDirectRouteTest {

    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;

    @BeforeEach
    void setUp() throws Exception {
        camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                    .to("mock:result");
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
    void testSendStringToDirectRoute() throws InterruptedException {
        String testMessage = "Hello Camel!";
        MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:result", MockEndpoint.class);
        mockEndpoint.expectedBodiesReceived(testMessage);

        // Send the message using ProducerTemplate to the direct:start endpoint
        producerTemplate.sendBody("direct:start", testMessage);

        // Assert that the mock endpoint received the message
        mockEndpoint.assertIsSatisfied();

        // Optionally, retrieve the exchange and check its content
        Exchange receivedExchange = mockEndpoint.getReceivedExchanges().get(0);
        String receivedBody = receivedExchange.getIn().getBody(String.class);
        assertEquals(testMessage, receivedBody, "The received message body should match the sent message.");
    }
}
