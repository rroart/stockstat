/**
 * 
 */
/**
 * @author roart
 *
 */
module common.model {
    exports roart.common.model;
    exports roart.common.pipeline.model;
    exports roart.common.pipeline.data;
    exports roart.common.pipeline.util;
    exports roart.result.model;
    requires common.constants;
    requires common.util;
    requires evolution.model;
    //requires jackson.annotations;
    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;
    requires common.config;
    requires iclij.common.config;
}
