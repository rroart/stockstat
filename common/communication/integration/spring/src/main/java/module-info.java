/**
 * 
 */
/**
 * @author roart
 *
 */
module common.communication.integration.spring {
    exports roart.common.communication.integration.spring;
    requires common.communication.integration.model;
    requires spring.rabbit;
    requires spring.context;
    requires spring.amqp;
    requires org.apache.commons.lang3;
    requires tools.jackson.databind;
    requires com.rabbitmq.client;
    requires common.util;
    requires common.constants;
    requires org.slf4j;
    requires spring.beans;
}
