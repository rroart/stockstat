/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.model {
    exports roart.iclij.model;
    exports roart.iclij.model.parse;
    exports roart.service.model;

    requires common.model;
    requires common.util;
    requires db;
    requires common.config;
    requires org.apache.commons.lang3;
    requires slf4j.api;
}
