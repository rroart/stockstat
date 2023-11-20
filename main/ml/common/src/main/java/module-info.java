/**
 * 
 */
/**
 * @author roart
 *
 */
module ml.common {
    exports roart.ml.model;
    exports roart.ml.common;

    requires iclij.common.config;
    requires common.config;
    requires common.constants;
    requires pipeline.model;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.lang3;
    requires common.util;
}
