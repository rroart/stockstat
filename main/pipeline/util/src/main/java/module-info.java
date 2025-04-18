/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline.util {
    exports roart.common.pipeline.util;

    requires common.config;
    requires iclij.common.config;
    requires common.constants;
    requires common.model;
    requires common.util;
    requires common.inmemory.model;
    requires curator.framework;
    requires commons.math3;
    requires model;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires stockstat.shadow.zookeeper;
    // for test
    //requires evolution.model;
}
