/**
 * 
 */
/**
 * @author roart
 *
 */
module common.communication.rest {
    exports roart.common.communication.rest;
    requires common.communication.model;

    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires org.slf4j;
    requires spring.core;
    requires spring.web;
    requires common.constants;
    requires common.util;
    requires common.webflux;
    requires org.apache.commons.lang3;
}
