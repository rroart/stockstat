package roart.common.queue;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roart.iclij.config.IclijConfig;

public class Queues {
	
	private static Logger log = LoggerFactory.getLogger(Queues.class);
	
    static final int limit = 100;

    private IclijConfig nodeConf;

    private Object controlService;

    public Queues(IclijConfig nodeConf, Object controlService) {
        super();
        this.nodeConf = nodeConf;
        this.controlService = controlService;
    }
    //public static Queue<TikaQueueElement> tikaRunQueue = new ConcurrentLinkedQueue<TikaQueueElement>();

    /*
    public MyQueue<QueueElement> getConvertQueue() {
        String queueid = QueueUtil.getConvertQueue();
        MyQueue<QueueElement> queue = MyQueues.get(queueid, nodeConf, controlService.curatorClient);
        return queue;
    }

     */

    private static volatile AtomicInteger tikas = new AtomicInteger(0);
    private static volatile AtomicInteger others = new AtomicInteger(0);
    private static volatile AtomicInteger indexs = new AtomicInteger(0);
    private static volatile AtomicInteger clients = new AtomicInteger(0);
    
    public static int getTikas() {
    	return tikas.get();
    }
    
    public static int getIndexs() {
    	return indexs.get();
    }
    
    public static int getOthers() {
    	return others.get();
    }
    
    public static int getClients() {
    	return clients.get();
    }
    
    public static void incTikas() {
    	tikas.incrementAndGet();
    }
    
    public static void incOthers() {
    	others.incrementAndGet();
    }
    
   public static void incIndexs() {
    	indexs.incrementAndGet();
    }
    
   public static void decTikas() {
   	tikas.decrementAndGet();
   }
   
   public static void decOthers() {
   	others.decrementAndGet();
   }
   
  public static void decIndexs() {
   	indexs.decrementAndGet();
   }
   
   public static void decClients() {
   	clients.decrementAndGet();
   }
   
    public static void incClients() {
    	clients.incrementAndGet();
    }

    public static void resetTikas() {
    	tikas = new AtomicInteger(0);
    }
    
    public static void resetOthers() {
    	others = new AtomicInteger(0);
    }
    
    public static void resetIndexs() {
    	indexs = new AtomicInteger(0);
    }
    
    public static void resetClients() {
    	clients = new AtomicInteger(0);
    }
    
}
