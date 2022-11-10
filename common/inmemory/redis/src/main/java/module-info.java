/**
 * 
 */
/**
 * @author roart
 *
 */
module common.inmemory.redis {
    exports roart.common.inmemory.redis;
    requires redis.clients.jedis;
    requires common.inmemory.model;
    requires common.constants;
}
