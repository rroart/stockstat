/**
 * 
 */
/**
 * @author roart
 *
 */
module mldao {
    exports roart.ml.dao;

    requires iclij.common.config;
    requires common.config;
    requires ml.common;
    requires ml.spark;
    requires ml.tensorflow;
    requires pipeline.model;
    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires ml.pytorch;
    requires ml.gem;
}
