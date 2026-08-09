package roart.common.communication.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Channel;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.springrabbit.SpringRabbitMQComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.integration.model.IntegrationCommunication;
import roart.common.util.JsonUtil;
import roart.common.constants.Constants;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Camel extends IntegrationCommunication {

    CamelContext context;
    ProducerTemplate producer;
    ConsumerTemplate consumer;
    String vhost = "task";
    String parsedConnection;

    // Define a unique name for your Camel component
    //private final String componentName = "myrabbitmq";
    private final String componentName = "spring-rabbitmq"; //"RMQ_CAMEL_CONSUMER";

    public Camel(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);

        parsedConnection = connection;

        context = new DefaultCamelContext();

        log.info("Components " + context.getComponentNames());

        // Create and register SpringRabbitMQComponent (do not use try-with-resources as it closes the component)
        SpringRabbitMQComponent component = new SpringRabbitMQComponent();
        component.setConnectionFactory(connectionFactory());
        component.setTestConnectionOnStartup(true);
        component.setAutoStartup(true);
        
        context.addComponent("spring-rabbitmq", component);

        log.info("Components " + context.getComponentNames());

        try {
            // Add routes to connect producer and consumer
            context.addRoutes(new RabbitMQRouteBuilder(this));
        } catch (Exception e) {
            log.error("Error adding routes: {}", Constants.EXCEPTION, e);
        }

        context.start();
        context.getRegistry().bind("rmq", connectionFactory());

        if (send) {
            producer = context.createProducerTemplate();
            // Use direct:send as the producer endpoint - the route will forward to RabbitMQ
            Endpoint sendEndpoint = context.getEndpoint("direct:send?block=false&failIfNoConsumers=false");
            producer.setDefaultEndpoint(sendEndpoint);
        }

        if (receive) {
            consumer = context.createConsumerTemplate();
        }
    }

    /*
    public Camel(String queue) {
        context = new DefaultCamelContext();
        AMQPComponent amqpComponent = AMQPComponent.amqpComponent("amqp://localhost:5672");
        context.addComponent("amqp", amqpComponent);

    }
    public Camel(String vhost, String queue) {
        context = new DefaultCamelContext();
        context.start();
        endpoint = context.getEndpoint("rabbitmq://localhost:5672/" + vhost + "?autoDelete=false&routingKey=camel&queue=" + queue);
        producer = context.createProducerTemplate();
        producer.setDefaultEndpoint(endpoint);
        consumer = context.createConsumerTemplate();
    }
     */

    public void send(String s) {
        log.info("Sending message to RabbitMQ via Camel");
        try {
            producer.sendBody(s);
            log.info("Message sent successfully");
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Override
    public String[] receiveString() {
        Endpoint endpoint = context.getEndpoint("direct:receive");
        Exchange receive = consumer.receive(endpoint, 5000);
        if (receive == null) {
            return new String[] { };
        }
        return new String[] { receive.getIn().getBody(String.class) };
    }

    @Override
    public String[] receiveStringAndStore() {
        Endpoint endpoint = context.getEndpoint("direct:receive");
        
        Exchange receive = null;
        try {
            receive = consumer.receive(endpoint, 5000);
            
            if (receive == null) {
                return new String[] { };
            }
            
            String body = null;
            try {
                body = receive.getIn().getBody(String.class);
                
                boolean stored = false;
                try {
                    stored = storeMessage.apply(body);
                } catch (Exception e) {
                    log.error("Error storing message, will redeliver: {}", e.getMessage(), e);
                    receive.setException(e);
                    return new String[] { };
                }
                
                if (stored) {
                    receive.getUnitOfWork().done(receive);
                    return new String[] { body };
                } else {
                    receive.setException(new RuntimeException("Message not stored"));
                    return new String[] { };
                }
            } catch (Exception e) {
                log.error("Error processing message, redelivering: {}", e.getMessage(), e);
                try {
                    receive.setException(e);
                } catch (Exception setExError) {
                    log.error("Failed to set exception on exchange: {}", setExError.getMessage(), setExError);
                }
                return new String[] { };
            }
        } catch (Exception e) {
            log.error("Error receiving message: {}", e.getMessage(), e);
            return new String[] { };
        }
    }

    public void destroy() {
        context.stop();
    }

    //@Bean
    public ConnectionFactory connectionFactory() {
        AbstractConnectionFactory connectionFactory =
            new CachingConnectionFactory(parsedConnection);
        connectionFactory.setUsername("username");
        connectionFactory.setPassword("password");
        //connectionFactory.set
        //connectionFactory.set
        if (sendreceive) {
            AmqpAdmin admin = new RabbitAdmin(connectionFactory);
            admin.declareQueue(new Queue(getSendService(), true));
            admin.declareQueue(new Queue(getReceiveService(), false));
        } else {
            if (send) {
                AmqpAdmin admin = new RabbitAdmin(connectionFactory);
                admin.declareQueue(new Queue(getSendService(), true));
                //admin.declareQueue(new Queue("camel"));
            }
            if (receive) {
                AmqpAdmin admin = new RabbitAdmin(connectionFactory);
                admin.declareQueue(new Queue(getReceiveService(), true));
            }
        }
        //connectionFactory.setUsername("guest");
        //connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    // github copilot

    public void printout() {
        printout(context);
    }

    public void printout(CamelContext ctx) {
        if (ctx == null) {
            log.warn("CamelContext is null, cannot print information");
            return;
        }

        log.info("========== CAMEL CONTEXT INFORMATION ==========");

        // Context basic info
        log.info("Context Name: {}", ctx.getName());
        log.info("Context State: {}", ctx.getStatus());
        log.info("Context Version: {}", ctx.getVersion());
        if (ctx.getManagementName() != null) {
            log.info("Management Name: {}", ctx.getManagementName());
        }

        // === ROUTES ===
        printRouteInfo(ctx);

        // === COMPONENTS ===
        printComponentInfo(ctx);

        // === ENDPOINTS ===
        printEndpointInfo(ctx);

        // === PRODUCER AND CONSUMER INFO ===
        printProducerConsumerInfo();

        // === REGISTRY ===
        printRegistryInfo(ctx);

        log.info("\n========== END CAMEL CONTEXT INFORMATION ==========\n");
    }

    private void printRouteInfo(CamelContext ctx) {
        log.info("\n--- ROUTES ---");
        try {
            java.util.List<org.apache.camel.Route> routes = ctx.getRoutes();
            log.info("Total Routes: {}", routes.size());
            if (!routes.isEmpty()) {
                for (org.apache.camel.Route route : routes) {
                    log.info("  Route ID: {}", route.getId());
                    log.info("    Consumer: {}", route.getConsumer());
                    log.info("    Endpoint: {}", route.getEndpoint());
                    log.info("    Processor: {}", route.getProcessor());
                }
            } else {
                log.info("  No routes configured");
            }
        } catch (Exception e) {
            log.warn("Error retrieving routes: {}", e.getMessage());
        }
    }

    private void printComponentInfo(CamelContext ctx) {
        log.info("\n--- COMPONENTS ---");
        try {
            java.util.Set<String> componentNames = ctx.getComponentNames();
            log.info("Total Components: {}", componentNames.size());
            for (String compName : componentNames) {
                org.apache.camel.Component component = ctx.getComponent(compName);
                log.info("  Component: {} ({})", compName, component.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.warn("Error retrieving components: {}", e.getMessage());
        }
    }

    private void printEndpointInfo(CamelContext ctx) {
        log.info("\n--- ENDPOINTS ---");
        try {
            org.apache.camel.spi.EndpointRegistry endpointRegistry = ctx.getEndpointRegistry();
            Collection<Endpoint> endpoints = endpointRegistry.values();// .getEndpoints();
            log.info("Total Endpoints: {}", endpoints.size());
            for (Endpoint endpoint : endpoints) {
                log.info("  URI: {}", endpoint.getEndpointUri());
                log.info("    Endpoint Class: {}", endpoint.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.warn("Error retrieving endpoints: {}", e.getMessage());
        }
    }

    private void printProducerConsumerInfo() {
        log.info("\n--- PRODUCERS AND CONSUMERS ---");
        try {
            if (producer != null) {
                log.info("  Producer Template exists");
                log.info("    Default Endpoint: {}", producer.getDefaultEndpoint());
                log.info("    Camel Context: {}", producer.getCamelContext().getName());
            } else {
                log.info("  Producer Template: Not initialized");
            }

            if (consumer != null) {
                log.info("  Consumer Template exists");
                log.info("    Camel Context: {}", consumer.getCamelContext().getName());
            } else {
                log.info("  Consumer Template: Not initialized");
            }
        } catch (Exception e) {
            log.warn("Error retrieving producer/consumer info: {}", e.getMessage());
        }
    }

    private void printRegistryInfo(CamelContext ctx) {
        log.info("\n--- REGISTRY ---");
        try {
            org.apache.camel.spi.Registry registry = ctx.getRegistry();
            log.info("  Registry Class: {}", registry.getClass().getSimpleName());

            // Try to get ConnectionFactory
            try {
                ConnectionFactory cf = registry.lookupByNameAndType("rmq", ConnectionFactory.class);
                if (cf != null) {
                    log.info("  ConnectionFactory 'rmq' found: {}", cf.getClass().getSimpleName());
                }
            } catch (Exception e) {
                log.debug("  No ConnectionFactory 'rmq' in registry");
            }
        } catch (Exception e) {
            log.warn("Error retrieving registry info: {}", e.getMessage());
        }
    }

    public void printexchanges() {
        printexchanges(context);
    }

    public void printexchanges(CamelContext ctx) {
        if (ctx == null) {
            log.warn("CamelContext is null, cannot print exchange information");
            return;
        }

        log.info("\n========== EXCHANGES AND RELATIONSHIPS ==========");

        try {
            // Get all endpoints from the registry
            org.apache.camel.spi.EndpointRegistry endpointRegistry = ctx.getEndpointRegistry();
            Collection<Endpoint> endpoints = endpointRegistry.values();

            log.info("Total Exchanges (Endpoints): {}", endpoints.size());

            for (Endpoint endpoint : endpoints) {
                printExchangeDetails(ctx, endpoint);
            }

        } catch (Exception e) {
            log.warn("Error retrieving exchange information: {}", e.getMessage());
        }

        log.info("\n========== END EXCHANGES AND RELATIONSHIPS ==========\n");
    }

    private void printExchangeDetails(CamelContext ctx, Endpoint endpoint) {
        try {
            log.info("\n--- Exchange: {} ---", endpoint.getEndpointUri());
            log.info("  Endpoint Class: {}", endpoint.getClass().getSimpleName());
            log.info("  Endpoint Key: {}", endpoint.getEndpointKey());

            // Find related routes
            printRelatedRoutes(ctx, endpoint);

        } catch (Exception e) {
            log.warn("Error processing exchange details: {}", e.getMessage());
        }
    }

    private void printRelatedRoutes(CamelContext ctx, Endpoint endpoint) {
        try {
            java.util.List<org.apache.camel.Route> routes = ctx.getRoutes();
            java.util.List<org.apache.camel.Route> relatedRoutes = new java.util.ArrayList<>();

            // Find routes that use this endpoint
            for (org.apache.camel.Route route : routes) {
                Endpoint routeEndpoint = route.getEndpoint();
                if (routeEndpoint != null && routeEndpoint.getEndpointUri().equals(endpoint.getEndpointUri())) {
                    relatedRoutes.add(route);
                }
            }

            if (!relatedRoutes.isEmpty()) {
                log.info("  Related Routes: {}", relatedRoutes.size());
                for (org.apache.camel.Route route : relatedRoutes) {
                    log.info("    Route ID: {}", route.getId());
                    log.info("      Consumer: {}", route.getConsumer());
                    log.info("      Processor: {}", route.getProcessor());
                }
            } else {
                log.info("  Related Routes: None");
            }
        } catch (Exception e) {
            log.debug("Error retrieving related routes: {}", e.getMessage());
        }
    }

}

class RabbitMQRouteBuilder extends RouteBuilder {
    private final Camel camel;

    public RabbitMQRouteBuilder(Camel camel) {
        this.camel = camel;
    }

    //@Override
    public void configure2() throws Exception {
        if (camel.send) {
            from("direct:send")
                    .to("spring-rabbitmq:amq.direct?routingKey=camel");
        }

        if (camel.receive) {
            from("spring-rabbitmq:amq.direct?routingKey=camel")
                    .to("direct:receive");
        }
    }

    @Override
    public void configure() throws Exception {
        if (camel.send) {
            from("direct:send?block=false&failIfNoConsumers=false")
                .to("spring-rabbitmq:amq.direct?routingKey=" + camel.getSendService() + "&queues=" + camel.getSendService() + "&args.durable=true&arg.durable=true");
        }

        if (camel.receive) {
            from("spring-rabbitmq:amq.direct?routingKey=" + camel.getReceiveService() + "&queues=" + camel.getReceiveService() + "&args.durable=true&arg.durable=true")
                .to("direct:receive");
        }
    }
}
