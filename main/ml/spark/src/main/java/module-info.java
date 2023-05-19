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

    requires iclij.common.config;
    requires common.config;
    requires common.constants;
    requires pipeline.common;
    requires scala.library;
    requires org.slf4j;
    requires stockstat.shadow.spark;
    requires ml.common;
    requires pipeline.model;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.lang3;
}
