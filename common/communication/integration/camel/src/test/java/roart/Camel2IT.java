package roart;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.component.amqp.AMQPComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Camel2IT {

    CamelContext context;

    @BeforeEach
    public void setup() {
        System.out.println("setup1");

        try {
            context.addRoutes(new RouteBuilder() {
                public void configure() {
                    //errorHandler(deadLetterChannel("file:deadletter"));
                    errorHandler(deadLetterChannel("amqp:queue:dead"));
                    from("amqp:queue:doc")
                    .convertBodyTo(String.class)
                    .to("amqp:queue:incomingDoc");
                    from("amqp:queue:incomingDoc")
                    //.convertBodyTo(TikaQueueElement.class)
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception 
{
                            //System.out.println("fail " + exchange.isFailed());
                            //System.out.println("trans " + exchange.isTransacted());
                            System.out.println("We just got: "
                                    + exchange.getIn().getBody(String.class));
                        }
                    })
                    //.convertBodyTo(TikaQueueElement.class)
                    //bean(TikaQueueElement.class, "doSomething(\"t\")") 
                    .to("jms:incomingOrders");       
                    from("amqp:queue:incomingDocNot").
                    process(new Processor() {
                        public void process(Exchange exchange) throws Exception 
{
                            System.out.println("We just got not: "
                                    + exchange.getIn().getBody());
                        }
                    }).
                    to("amqp:queue:dead").
                    process(new Processor() {
                        public void process(Exchange exchange) throws Exception 
{
                            System.out.println("exchange here");
                            System.out.println("We just got not: "
                                    + exchange.getIn().getBody());
                        }
                    });
                }
            });
            System.out.println("setup2");
            context.start();
            System.out.println("setup3");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("setup4");

    }

    // TODO @Test
    public void test() {
        Endpoint endpoint = context.getEndpoint("amqp:queue:doc");
        Exchange exchange = endpoint.createExchange();
        // populate exchange with data
        //getProcessor().process(exchange);
        ProducerTemplate template = exchange.getContext().createProducerTemplate();
        template.sendBody("amqp:queue:doc", "<hello>world!</hello>");
        template.sendBody("amqp:queue:doc", "<hello>again!</hello>");
        template.sendBody("amqp:queue:incomingDoc", "<hello>again again!</hello>");
        template.sendBody("amqp:queue:incomingDoc2", "<hello>again 2!</hello>");

        /*
        ConsumerTemplate template2 = exchange.getContext().createConsumerTemplate();
        Exchange str = template2.receive ("amqp:queue");  
        Message str2 = str.getMessage();
        System.out.println("msg " + str2.getBody());
         */
    }


}
