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
    requires org.slf4j;
    requires java.desktop;
    requires common.inmemory.model;
    requires common.constants;
}
