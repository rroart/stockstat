/**
 * 
 */
/**
 * @author roart
 *
 */
module common.communication.message.pulsar {
    exports roart.common.communication.message.pulsar;
    requires common.communication.message.model;
    requires pulsar.client.api;
    //requires pulsar.common;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.databind;
    requires common.constants;
    //requires com.fasterxml.jackson.annotations;
    requires org.slf4j;
}
