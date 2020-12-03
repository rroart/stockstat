/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.service {
    exports roart.iclij.service;
    exports roart.iclij.model.component;

    requires iclij.common.config;
    requires common.config;
    requires common.service;
    requires common.util;
    requires slf4j.api;
    requires common.model;
    requires common.constants;
    requires common.eureka;
    requires common.communication.factory;
    requires org.apache.commons.lang3;
    requires iclij.common.model;
    requires spring.web;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires common.communication.model;
}
