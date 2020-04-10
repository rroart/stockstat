/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.service {
    exports roart.iclij.service;

    requires iclij.common.config;
    requires common.config;
    requires common.service;
    requires slf4j.api;
    requires common.model;
    requires common.constants;
    requires common.eureka;
    requires org.apache.commons.lang3;
    requires iclij.common.model;
    requires spring.web;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
}
