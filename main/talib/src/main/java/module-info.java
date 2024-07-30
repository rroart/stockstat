/**
 * 
 */
/**
 * @author roart
 *
 */
module talib {
    exports roart.talib;
    exports roart.talib.impl;
    exports roart.talib.util;

    requires common.constants;
    requires common.util;
    requires commons.math3;
    requires jcommon;
    requires jfreechart;
    requires slf4j.api;
    requires ta.lib;
    requires model;
    requires stockutil;
    requires ta4j.core;
    requires org.apache.commons.lang3;
    requires common.model;
}
