package roart.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.service.ServiceParam;

public class Queues {
	
	private static Logger log = LoggerFactory.getLogger(Queues.class);
	
    static final int limit = 100;

    //public static Queue<TikaQueueElement> tikaRunQueue = new ConcurrentLinkedQueue<TikaQueueElement>();

    public static volatile Queue<ServiceParam> clientQueue = new ConcurrentLinkedQueue<ServiceParam>();

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
