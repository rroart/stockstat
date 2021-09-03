/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.config {
    exports roart.iclij.config;

    requires common.config;
    requires commons.configuration2;
    requires com.fasterxml.jackson.core;
    requires java.xml;
    requires slf4j.api;
    requires com.fasterxml.jackson.databind;
    requires common.constants;
}
