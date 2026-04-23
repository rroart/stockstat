/**
 * 
 */
/**
 * @author roart
 *
 */
module common.io {
    exports roart.model.io;
    exports roart.model.io.util;

    requires common.communication.factory;
    requires common.filesystem.client;
    requires common.inmemory.factory;
    requires common.model;
    requires common.webflux;
    requires dbdao;
    requires iclij.common.config;
    requires curator.client;
    requires curator.framework;
    requires common.communication.model;
    requires common.inmemory.model;
    requires common.util;
    requires iclij.common.model;
    requires common.queue;
    requires tools.jackson.databind;
    requires org.apache.commons.lang3;
    requires common.constants;
    requires common.queueutil;
    requires org.slf4j;
}
