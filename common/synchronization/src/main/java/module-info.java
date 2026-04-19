/**
 * 
 */
/**
 * @author roart
 *
 */
module common.synchronization {
    exports roart.common.synchronization;
    exports roart.common.synchronization.impl;

    requires common.constants;
    requires common.zookeeper;
    requires curator.framework;
    requires curator.recipes;
    requires com.hazelcast.core;
    //requires slf4j.api;
    requires org.slf4j;
    requires curator.client;
    requires com.fasterxml.jackson.annotation;
    requires redisson;
}
