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
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires common.constants;
    requires com.fasterxml.jackson.annotation;
    requires spring.context;
    requires spring.beans;
}
