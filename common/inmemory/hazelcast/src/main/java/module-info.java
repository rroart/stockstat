/**
 * 
 */
/**
 * @author roart
 *
 */
module common.inmemory.hazelcast {
    exports roart.common.inmemory.hazelcast;
    requires com.hazelcast.core;
    requires slf4j.api;
    requires java.desktop;
    requires common.inmemory.model;
    requires common.constants;
}
