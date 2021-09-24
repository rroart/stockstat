/**
 * 
 */
/**
 * @author roart
 *
 */
module db {
    exports roart.db.model;
    exports roart.db.thread;

    requires common.constants;
    requires common.util;
    requires java.persistence;
    requires org.hibernate.orm.core;
    //requires org.slf4j;
    requires org.slf4j;
    requires java.naming;
    requires java.transaction;
	requires org.apache.commons.lang3;
}
