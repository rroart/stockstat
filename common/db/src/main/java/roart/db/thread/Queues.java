package roart.db.thread;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.query.Query;

import roart.db.model.HibernateUtil;

public class Queues {
    public static volatile Deque<Object> queue = new ConcurrentLinkedDeque<Object>();
    public static volatile Deque<String> queuedelete = new ConcurrentLinkedDeque<>();
    public static volatile Deque<Pair<HibernateUtil, Query>> queuedeleteq = new ConcurrentLinkedDeque<>();
}
