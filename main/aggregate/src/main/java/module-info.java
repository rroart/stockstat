/**
 * 
 */
/**
 * @author roart
 *
 */
module aggregate {
    exports roart.aggregator.impl;
    exports roart.aggregatorindicator.impl;
    
    requires iclij.common.config;
    requires common.config;
    requires common.constants;
    requires common.model;
    requires commons.math3;
    requires model;
    requires org.apache.commons.lang3;
    requires ml.common;
    requires pipeline.model;
    requires common.util;
    requires ta.lib;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires myexecutor;
    requires mldao;
    requires dbdao;
    requires com.fasterxml.jackson.core;
    requires talib;
    requires evolution.gene;
    requires pipeline.indicator;
    requires pipeline.common;
    requires stockutil;
    requires stocketl;
    requires ml.pytorch;
}
