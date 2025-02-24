/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.model {
    exports roart.iclij.model.action;
    exports roart.iclij.model.config;
    exports roart.iclij.model.parse;

    requires common.util;
    requires common.config;
    requires iclij.common.config;
    requires iclij.common.constants;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires common.constants;
}
