package org.spider_man.monitor;

import org.slf4j.Logger;
import org.spider_man.RequestTask;
import org.spider_man.spider.AbsSpider;
import org.spider_man.util.LoggerSpider;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class MonitorSpider implements Runnable {
    private final static Logger logger = LoggerSpider.getLogger(MonitorSpider.class, "logs/monitor.log");
    private LinkedBlockingQueue<RequestTask> queue;
    private AbsSpider spider;
    private ThreadPoolExecutor poolExecutor;

    public MonitorSpider(LinkedBlockingQueue<RequestTask> queue, AbsSpider spider, ThreadPoolExecutor poolExecutor) {
        this.queue = queue;
        this.spider = spider;
        this.poolExecutor = poolExecutor;
    }

    @Override
    public void run() {
        logger.info("监控启动。。。");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.error("" + e);
        }
        while (true) {
            if (queue.isEmpty() && poolExecutor.getActiveCount() == 0) {
                spider.isStop = true;
                logger.info("关闭监控。。。");
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("" + e);
                }
            }
            logger.info("当前队列数：" + queue.size() + "\n" + "当前激活线程数：" + poolExecutor.getActiveCount() + "\n" + "配置最大线程数：" + spider.configThreadNum());
        }
    }
}
