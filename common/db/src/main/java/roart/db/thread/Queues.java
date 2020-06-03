package roart.db.thread;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Queues {
    public static volatile Deque<Object> queue = new ConcurrentLinkedDeque<Object>();
}
