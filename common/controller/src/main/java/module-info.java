/**
 * 
 */
/**
 * @author roart
 *
 */
module common.controller {
    exports roart.common.controller;

    requires common.communication.factory;
    requires common.communication.model;
    requires common.constants;
    requires common.util;
    requires iclij.common.config;
    requires org.slf4j;
    requires guava;
    requires com.fasterxml.jackson.databind;
    requires dbdao;
    requires curator.client;
    requires common.queueutil;
    requires curator.framework;
}
