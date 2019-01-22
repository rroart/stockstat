package roart.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyExecutors {

    private static final Logger log = LoggerFactory.getLogger(MyExecutors.class);

    private static List<ThreadPoolExecutor> /*ExecutorService*/ pools = null;
    
    public static void init(double[] cpus) {
        if (pools != null) {
            return;
        }
        pools = new ArrayList<>();
        for (double cpu : cpus) {
            if (cpu == 0) {
                pools.add((ThreadPoolExecutor) Executors.newCachedThreadPool());
                continue;
            }
            int nThreads = (int) (Runtime.getRuntime().availableProcessors() * cpu);
            if (nThreads <= 10) {
                nThreads = 10;
            }
            log.info("nthreads {}", nThreads);
            ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
            pools.add(pool);
        }
    }
    
    public static <T> Future<T> run(Callable<T> callable, int pool) {
        return pools.get(pool).submit(callable);
    }
  
    public static void run(Runnable callable, int pool) {
        pools.get(pool).execute(callable);
    }
  
}