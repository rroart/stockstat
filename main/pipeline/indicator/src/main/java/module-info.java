/**
 * 
 */
/**
 * @author roart
 *
 */
open module pipeline.indicator {
    exports roart.indicator.impl;
    exports roart.indicator.util;
    exports roart.pipeline.impl;
    
    requires iclij.common.config;
    requires common.config;
    requires common.constants;
    requires commons.math3;
    requires model;
    requires pipeline.model;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires talib;
    requires stockutil;
    requires stocketl;
    requires ml.common;
    requires com.fasterxml.jackson.databind;
    requires common.util;
    requires common.model;
    requires dbdao;
    requires pipeline.util;
}
