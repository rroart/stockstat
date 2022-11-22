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
    requires common.webflux;
    requires org.slf4j;
    requires ml.common;
    requires pipeline.model;
    requires org.apache.commons.lang3;
    requires common.util;
    requires spring.web;
}
