/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline.predictor {
    exports roart.predictor.impl;

    requires common.constants;
    requires commons.math3;
    requires ml.common;
    requires mldao;
    requires org.apache.commons.lang3;
    requires pipeline.model;
    requires org.slf4j;
    requires stockutil;
    requires iclij.common.config;
    requires common.config;
    requires common.util;
    requires model;
    requires common.model;
    requires pipeline.indicator;
    requires com.fasterxml.jackson.databind;
}
