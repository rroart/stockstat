/**
 * 
 */
/**
 * @author roart
 *
 */
module common.communication.integration.camel {
    exports roart.common.communication.integration.camel;
    requires camel.api;
    requires camel.core.engine;
    requires common.communication.integration.model;
    requires common.util;
    requires camel.amqp;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.lang3;
    //requires slf4j.api;
}
