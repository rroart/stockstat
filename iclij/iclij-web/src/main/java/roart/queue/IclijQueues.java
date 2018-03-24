package roart.queue;

import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.service.IclijServiceParam;
import roart.service.ServiceParam;
import roart.util.Constants;

public class IclijQueues {
	
	private static Logger log = LoggerFactory.getLogger(IclijQueues.class);
	
    static final int limit = 100;

    //public static Queue<TikaQueueElement> tikaRunQueue = new ConcurrentLinkedQueue<TikaQueueElement>();

    public static volatile Queue<IclijServiceParam> clientQueue = new ConcurrentLinkedQueue<>();

    private static volatile AtomicInteger clients = new AtomicInteger(0);
 
    public static int getClients() {
    	return clients.get();
    }
    
   public static void decClients() {
   	clients.decrementAndGet();
   }
   
    public static void incClients() {
    	clients.incrementAndGet();
    }

    public static void resetClients() {
    	clients = new AtomicInteger(0);
    }
    
}
