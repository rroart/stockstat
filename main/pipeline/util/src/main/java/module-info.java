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
    requires tools.jackson.databind;
    requires stockstat.shadow.zookeeper;
    requires org.checkerframework.checker.qual;
    requires org.apache.commons.lang3;
    requires org.jspecify;
    // for test
    //requires evolution.model;
}
