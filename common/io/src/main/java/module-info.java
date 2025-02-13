/**
 * 
 */
/**
 * @author roart
 *
 */
module common.io {
    exports roart.model.io;

    requires common.communication.factory;
    requires common.filesystem.client;
    requires common.inmemory.factory;
    requires common.model;
    requires common.webflux;
    requires dbdao;
    requires iclij.common.config;
    requires curator.client;
    requires curator.framework;
}
