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

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires slf4j.api;
    requires spring.core;
    requires spring.web;
    requires common.constants;
    requires common.util;
    requires org.apache.commons.lang3;
}
