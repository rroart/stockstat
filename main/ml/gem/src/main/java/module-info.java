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
    requires common.webflux;
    requires ehcache;
    requires slf4j.api;
    requires ml.common;
    requires pipeline.model;
    requires org.apache.commons.lang3;
    requires model;
    requires common.constants;
    requires org.apache.commons.codec;
}
