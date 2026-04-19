package roart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roart.common.constants.Constants;
import roart.common.synchronization.MySemaphore;
import roart.common.util.TimeUtil;
import roart.core.service.CoreControlService;
import roart.db.thread.Queues;
import roart.iclij.config.IclijConfig;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class QueueRunner implements Runnable {
    private static Logger log = LoggerFactory.getLogger(QueueRunner.class);

    public static volatile int timeout = 3600;

    final int NTHREDS = 2;

    private IclijConfig config;

    private CoreControlService controlService;

    public QueueRunner(IclijConfig nodeConf, CoreControlService controlService) {
        super();
        this.config = nodeConf;
        this.controlService = controlService;
    }

    public IclijConfig getConfig() {
        return config;
    }

    public void setConfig(IclijConfig config) {
        this.config = config;
    }

    public void run() {
        /*
        int nThreads = 1; // TODO config
        int running = 0;
        log.info("nthreads {}", nThreads);

        for(int i = running; i < nThreads; i++) {
            Runnable run = () -> {
                Queue<MySemaphore> semaphores = new ConcurrentLinkedQueue<>();
                while (true) {
                    try {
                        unlockSemaphores(semaphores);
                        if (new Queues(config, controlService).getConvertQueueSize() == 0) {
                            log.debug("Convert queue empty, sleeping");
                            TimeUtil.sleep(10);
                            continue;
                        }
                        if (new Queues(config, controlService).indexQueueHeavyLoaded()) {
                            log.info("Index queue heavy loaded, sleeping");
                            TimeUtil.sleep(1);
                            continue;
                        }
                        new Queues(config, controlService).incConverts();
                        doConvertTimeout(config, semaphores);
                        new Queues(config, controlService).decConverts();
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                        TimeUtil.sleep(10);
                    }
                }
            };
            new Thread(run).start();
        }
        try {
            TimeUnit.DAYS.sleep(1000);
            return;
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
        }

         */
    }

    private void unlockSemaphores(Queue<MySemaphore> locks) {
        if (!locks.isEmpty()) {
            log.info("unlock");
        }
        while (!locks.isEmpty()) {
            MySemaphore lock = locks.poll();
            lock.unlock();
            if (locks.isEmpty()) {
                log.info("unlock");
            }
        }
    }
}
