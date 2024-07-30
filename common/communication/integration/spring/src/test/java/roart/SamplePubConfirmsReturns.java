package roart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Starts the Spring Context and will initialize the Spring Integration message flow.
 *
 * @author Gary Russell.
 * @since 4.0
 *
 */
public class SamplePubConfirmsReturns {

    private static final Log LOGGER = LogFactory.getLog(SamplePubConfirmsReturns.class);

    //private SamplePubConfirmsReturns() { }

    /**
     * Load the Spring Integration Application Context
     *
     * @param args - command line arguments
     */
    @Test
    public void main() {

        LOGGER.info("\n========================================================="
                + "\n                                                         "
                + "\n          Welcome to Spring Integration!                 "
                + "\n                                                         "
                + "\n    For more information please visit:                   "
                + "\n    https://www.springsource.org/spring-integration       "
                + "\n                                                         "
                + "\n=========================================================" );

        @SuppressWarnings("resource")
        final AbstractApplicationContext context =
        new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/spring-integration-confirms-context.xml");

        context.registerShutdownHook();

        LOGGER.info("\n========================================================="
                + "\n                                                          "
                + "\n    This is the AMQP Sample with confirms/returns -       "
                + "\n                                                          "
                + "\n    Please enter some text and press return. The entered  "
                + "\n    Message will be sent to the configured RabbitMQ Queue,"
                + "\n    then again immediately retrieved from the Message     "
                + "\n    Broker and ultimately printed to the command line.    "
                + "\n    Send 'fail' to demonstrate a return because the       "
                + "\n    message couldn't be routed to a queue.                "
                + "\n    Send 'nack' to demonstrate a NACK because the         "
                + "\n    exchange doesn't exist, causing the channel to be     "
                + "\n    closed in error by the broker.                        "
                + "\n                                                          "
                + "\n=========================================================" );

    }
}