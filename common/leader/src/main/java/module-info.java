/**
 * 
 */
/**
 * @author roart
 *
 */
module common.leader {
    exports roart.common.leader;
    exports roart.common.leader.impl;
    
    requires iclij.common.config;
    requires common.constants;
    requires common.util;
    requires curator.client;
    requires curator.framework;
    requires curator.recipes;
    requires com.hazelcast.core;
    requires redis.clients.jedis;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires org.slf4j;
}
