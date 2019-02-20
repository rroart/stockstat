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

    requires common.config;
    requires common.constants;
    requires pipeline.model;
    requires slf4j.api;
    requires com.fasterxml.jackson.databind;
}
