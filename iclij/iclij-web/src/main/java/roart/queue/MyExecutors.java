package roart.queue;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyExecutors {

    private static final Logger log = LoggerFactory.getLogger(MyExecutors.class);

    private static ThreadPoolExecutor /*ExecutorService*/ pool = null;
    
    public static void init(int fraction) {
        if (pool != null) {
            return;
        }
        if (fraction < 1) {
            fraction = 1;
        }
        int nThreads = Runtime.getRuntime().availableProcessors() / fraction;
        if (nThreads == 0) {
            nThreads = 1;
        }
        log.info("nthreads {}", nThreads);
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
    }
    
    public static <T> Future<T> run(Callable<T> callable) {
        return pool.submit(callable);
    }
  
    public static void run(Runnable callable) {
        pool.execute(callable);
    }
  
}