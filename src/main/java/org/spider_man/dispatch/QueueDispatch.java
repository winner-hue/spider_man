package org.spider_man.dispatch;

import org.slf4j.Logger;
import org.spider_man.RequestTask;
import org.spider_man.execute.SpiderExecute;
import org.spider_man.monitor.MonitorSpider;
import org.spider_man.spider.QueueSpider;
import org.spider_man.util.CommentUtil;
import org.spider_man.util.LoggerSpider;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QueueDispatch {
    private static Logger logger = LoggerSpider.getLogger(QueueDispatch.class);

    protected void monitor(LinkedBlockingQueue<RequestTask> queue, QueueSpider spider, ThreadPoolExecutor poolExecutor) {
        Thread thread = new Thread(new MonitorSpider(queue, spider, poolExecutor));
        thread.setName("monitor");
        thread.start();
    }

    protected LinkedBlockingQueue<RequestTask> getQueue(QueueSpider spider) {
        return new LinkedBlockingQueue<>(spider.getQueueSize());
    }

    protected BlockingQueue<Runnable> getBlockQueue(QueueSpider spider) {
        return new LinkedBlockingQueue<>(spider.configThreadNum());
    }

    protected ThreadPoolExecutor getPoolExecutor(QueueSpider spider, BlockingQueue<Runnable> blockingQueue) {
        return new ThreadPoolExecutor(spider.configThreadNum(), spider.configThreadNum(), 20, TimeUnit.SECONDS, blockingQueue);
    }

    protected void process(QueueSpider spider, LinkedBlockingQueue<RequestTask> queue, BlockingQueue<Runnable> blockingQueue, ThreadPoolExecutor poolExecutor, HashSet<String> set) {
        while (!spider.isStop()) {
            while (spider.getRequestTasks().size() > 0) {
                if (queue.size() == spider.getQueueSize()) {
                    if (spider.getRequestTasks().size() / Double.parseDouble(String.valueOf(spider.getQueueSize())) > 0.3) {
                        logger.warn("爬虫队列任务数过多： 队列任务：" + queue.size() + "， 待下发任务：" + spider.requestTasks.size());
                    }
                    break;
                }
                RequestTask requestTask = spider.getRequestTasks().remove(0);
                if (spider.configTaskDup()) {
                    String url = requestTask.getUrl();
                    String md5Url = CommentUtil.Md5(url);
                    if (set.contains(md5Url)) {
                        continue;
                    }
                    set.add(md5Url);
                    queue.add(requestTask);
                    continue;
                }
                queue.add(requestTask);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (queue.isEmpty()) {
                continue;
            }
            try {
                RequestTask take = queue.take();
                SpiderExecute spiderExecute = new SpiderExecute(take, spider);
                Thread spiderThread = new Thread(spiderExecute);
                poolExecutor.execute(spiderThread);
            } catch (Exception e) {
                logger.error("提取任务失败：", e);
            }

        }
        poolExecutor.shutdown();
    }

    protected void run(QueueSpider spider) {
        LinkedBlockingQueue<RequestTask> queue = getQueue(spider);
        BlockingQueue<Runnable> blockingQueue = getBlockQueue(spider);
        ThreadPoolExecutor poolExecutor = getPoolExecutor(spider, blockingQueue);
        spider.beforeSpider(spider);
        monitor(queue, spider, poolExecutor);
        HashSet<String> set = new HashSet<>();
        process(spider, queue, blockingQueue, poolExecutor, set);
    }

    public void runScript(QueueSpider spider) {
        run(spider);
        spider.afterSpider(spider);
    }

}
