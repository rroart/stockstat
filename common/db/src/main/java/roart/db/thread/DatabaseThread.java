package roart.db.thread;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.db.model.HibernateUtil;
import org.hibernate.query.Query;

public class DatabaseThread extends Thread {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    static final int update = 2;
    static long lastupdate = 0;

    public void run() {
        while (true) {
            HibernateUtil hu = new HibernateUtil(true);
            try {
                hu.openSessionWrite();
                hu.beginTransactionWrite();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            long now = System.currentTimeMillis();
            log.debug("Updatetime {}", (int) ((now - lastupdate)/1000));
            Object object = null;
            object = Queues.queue.poll();
            if (true || (now - lastupdate) >= update * 1000) {
                lastupdate = System.currentTimeMillis();
            }
            while (object != null) {
                try {
                    hu.save(object);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                object = Queues.queue.poll();
            }
            String string = null;
            string = Queues.queuedelete.poll();
            while (string != null) {
                try {
                    hu.delete(string);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                string = Queues.queuedelete.poll();
            }
            Pair<HibernateUtil, Query> queryPair = Queues.queuedeleteq.poll();
            while (queryPair != null) {
                try {
                	HibernateUtil aHibernateUtil = queryPair.getLeft();
                    aHibernateUtil.delete(queryPair.getRight());
                    aHibernateUtil.commit();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                queryPair = Queues.queuedeleteq.poll();
            }
            try {
                hu.commit();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            try {
                int sleepsec = update - (int) ((lastupdate - now)/1000);  
                if (sleepsec < 1) {
                    sleepsec = 1;
                }
                log.debug("Sleep seconds {}", sleepsec);
                TimeUnit.SECONDS.sleep(sleepsec);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
                Thread.currentThread().interrupt();
            }
        }
        
    }
}
