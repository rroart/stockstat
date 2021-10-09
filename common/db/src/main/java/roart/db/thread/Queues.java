package roart.db.thread;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.query.Query;

import roart.db.model.HibernateUtil;

public class Queues {
    public static final Deque<Object> queue = new ConcurrentLinkedDeque<Object>();
    public static final Deque<String> queuedelete = new ConcurrentLinkedDeque<>();
    public static final Deque<Pair<HibernateUtil, Query>> queuedeleteq = new ConcurrentLinkedDeque<>();
}
