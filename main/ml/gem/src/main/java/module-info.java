/**
 * 
 */
/**
 * @author roart
 *
 */
module ml.gem {
    exports roart.ml.gem;

    requires common.config;
    requires common.eureka;
    requires ehcache;
    requires slf4j.api;
    requires ml.common;
    requires pipeline.model;
    requires org.apache.commons.lang3;
}
