/**
 * 
 */
/**
 * @author roart
 *
 */
module mldao {
    exports roart.ml.dao;

    requires common.config;
    requires ml.common;
    requires ml.spark;
    requires ml.tensorflow;
    requires pipeline.model;
    requires slf4j.api;
    requires org.apache.commons.lang3;
}
