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
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires common.communication.integration.spring;
    requires common.communication.message.pulsar;
    requires common.communications.message.kafka;
    requires common.constants;
    requires common.webflux;
}
