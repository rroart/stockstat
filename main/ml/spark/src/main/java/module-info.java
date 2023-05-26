/**
 * 
 */
/**
 * @author roart
 *
 */
module ml.spark {
    exports roart.ml.spark;

    requires iclij.common.config;
    requires common.config;
    requires common.constants;
    requires common.webflux;
    requires pipeline.common;
    requires scala.library;
    requires org.slf4j;
    requires ml.common;
    requires pipeline.model;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.lang3;
}
