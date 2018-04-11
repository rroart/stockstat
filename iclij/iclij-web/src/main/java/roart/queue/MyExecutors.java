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
    
    public static void init(double cpu) {
        if (pool != null) {
            return;
        }
        int nThreads = (int) (Runtime.getRuntime().availableProcessors() * cpu);
        if (nThreads <= 10) {
            nThreads = 10;
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