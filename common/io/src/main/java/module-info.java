/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.model {
    exports roart.model.io;

    requires iclij.common.config;
    requires dbdao;
    requires common.model;
    requires common.inmemory.factory;
    requires common.communication.factory;
    requires common.webflux;
    requires common.filesystem.client;
}
