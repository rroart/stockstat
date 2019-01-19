/**
 * 
 */
/**
 * @author roart
 *
 */
module ml.spark {
    exports roart.ml.spark;
    exports roart.ml.spark.util;

    requires common.config;
    requires common.constants;
    requires pipeline.common;
    requires scala.library;
    requires slf4j.api;
    requires stockstat.shadow.spark;
    requires ml.common;
    requires pipeline.model;
}
