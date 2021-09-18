package roart.db.thread;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.hibernate.query.Query;

public class Queues {
    public static volatile Deque<Object> queue = new ConcurrentLinkedDeque<Object>();
    public static volatile Deque<String> queuedelete = new ConcurrentLinkedDeque<>();
    public static volatile Deque<Query> queuedeleteq = new ConcurrentLinkedDeque<>();
}
