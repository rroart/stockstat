package roart;

import org.apache.camel.component.activemq.ActiveMQComponent;
//import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
//import javax.jms.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.integration.camel.Aclass;
import roart.common.communication.integration.camel.Camel;
import roart.testdata.TestUtils;
import roart.testdata.TestConfiguration;

// this is @ExtendWith(SpringExtension.class) and @ContextConfiguration 
@SpringJUnitConfig
@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
@ComponentScan(basePackages = "roart.testdata")
@SpringBootTest(classes = TestConfiguration.class)
public class CamelIT {

    /*
    @Test
    public void main() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {
            context.addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("activemq:queue:test.queue")
                    .to("stream:out");
                }
            });
            ProducerTemplate template = context.createProducerTemplate();
            context.start();
            template.sendBody("activemq:test.queue", "Hello World");
            Thread.sleep(2000);
        } finally {
            context.stop();
        }
    }
    */

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestConfiguration config;
    
    //@Autowired
    //private TestUtils testUtils;
    
    @Test
    public void camelRabbitMq() throws Exception {
        System.out.println("1");
        Camel camel = new Camel("CAMEL", String.class, "testqueue", new ObjectMapper(), true, true, false, config.getServerCamel(), null);
        System.out.println("1");
        camel.send("one");
        System.out.println("1");
        camel.send("two");
        camel.send("three");
        camel.send("four");
        camel.send("done");

        String[] body = new String[] { null };
        while (!"done".equals(body[0])) {
            body = camel.receiveString();
            System.out.println("b"+body[0]);
        }
        System.out.println("2");

        camel.destroy();
    }

    @Test
    public void camelRabbitMq2() throws Exception {
        Camel camel = new Camel("CAMEL", Aclass.class, "tasks", new ObjectMapper(), true, true, false, config.getServerCamel(), null);
        camel.send(new Aclass("one", 1));
        camel.send(new Aclass("two", 2));
        System.out.println("11");

        Object[] body = null;
        while (body == null || !"two".equals(((Aclass)body[0]).getS())) {
            body = camel.receive();
            System.out.println("3"+ body);
        }
        System.out.println("33");

        camel.destroy();
    }

}
