/**
 * 
 */
/**
 * @author roart
 *
 */
module ml.tensorflow {
    exports roart.ml.tensorflow;

    requires common.config;
    requires common.eureka;
    requires ehcache;
    requires slf4j.api;
    requires ml.common;
    requires pipeline.model;
    requires org.apache.commons.lang3;
    requires common.util;
}
