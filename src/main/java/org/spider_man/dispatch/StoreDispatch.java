package org.spider_man.dispatch;

import org.slf4j.Logger;
import org.spider_man.execute.MysqlStoreExecute;
import org.spider_man.spider.MysqlSpider;
import org.spider_man.util.LoggerSpider;
import org.spider_man.util.MysqlUtil;

public class StoreDispatch extends QueueDispatch {
    private static Logger logger = LoggerSpider.getLogger(StoreDispatch.class);

    public StoreDispatch() {
    }

    public void runScript(MysqlSpider spider) {
        MysqlUtil.initMysqlClient(spider);
        Thread thread = new Thread(new MysqlStoreExecute(spider));
        thread.setName("store");
        thread.start();
        run(spider);
        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.error("异常终止：" + e);
        }
        MysqlUtil.releaseMysqlClient();
        spider.afterSpider(spider);
    }

}
