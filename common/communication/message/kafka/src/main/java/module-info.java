/**
 * 
 */
/**
 * @author roart
 *
 */
module common.communications.message.kafka {
    exports roart.common.communication.message.kafka;
    requires common.communication.message.model;
    requires kafka.clients;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.codec;
    requires org.slf4j;
    requires common.constants;
}
