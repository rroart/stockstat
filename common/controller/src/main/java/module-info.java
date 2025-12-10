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
    requires common.io;
    requires common.util;
    requires common.filesystem.client;
    requires iclij.common.config;
    requires org.slf4j;
    requires guava;
    requires tools.jackson.databind;
    requires dbdao;
    requires curator.client;
    requires common.queueutil;
    requires curator.framework;
    requires org.apache.commons.lang3;
}
