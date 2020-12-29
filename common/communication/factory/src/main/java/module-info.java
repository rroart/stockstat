/**
 * 
 */
/**
 * @author roart
 *
 */
module common.communication.factory {
    exports roart.common.communication.factory;
    requires common.communication.model;
    requires common.communication.rest;
    requires common.communication.integration.camel;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires common.communication.integration.spring;
    requires common.communication.message.pulsar;
    requires common.communications.message.kafka;
    requires common.constants;
    //requires com.fasterxml.jackson.datatype.jsr310;
}
