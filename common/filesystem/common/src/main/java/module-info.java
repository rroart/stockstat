/**
 * 
 */
/**
 * @author roart
 *
 */
module common.filesystem {
    exports roart.common.filesystem;
    exports roart.common.filesystem.util;

    requires iclij.common.config;
    requires common.config;
    requires common.model;
    requires common.inmemory.model;
    requires com.fasterxml.jackson.annotation;
    requires common.constants;
    requires org.slf4j;
    requires org.apache.commons.lang3;
}
