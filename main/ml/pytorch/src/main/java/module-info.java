/**
 * 
 */
/**
 * @author roart
 *
 */
module ml.pytorch {
    exports roart.ml.pytorch;

    requires common.config;
    requires common.webflux;
    requires ehcache;
    requires slf4j.api;
    requires ml.common;
    requires pipeline.model;
    requires org.apache.commons.lang3;
    requires common.util;
    requires spring.web;
}
