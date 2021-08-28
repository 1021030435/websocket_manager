package org.jeecg.websocket.task;

/**
 * @author: Zz Ai
 * @date: 2021-08-05 15:20
 **/

import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.jeecg.common.util.LocalDateTimeUtils;
import org.jeecg.websocket.manager.WebsocketConnect;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class AbstractWebsocketTask implements Runnable {

    public String cacheResult;
    public String url;
    // 初始化状态为 0 未执行 1执行中
    public AtomicInteger taskStatus = new AtomicInteger(0);
    public LocalDateTime cacheUpdateTime;
    public LocalDateTime taskStartTime;
    public Integer restartTaskEval = 30;// 重新启动时间秒数 默认30执行一次
    private Object[] param;
    public CopyOnWriteArrayList<WebsocketConnect> connectList = new CopyOnWriteArrayList();

    // 此方法保证原子化执行
    public void doAndCacheUpdate() {
        // 当任务执行完之后才会释放状态
        if (taskStatus.compareAndSet(0, 1)) {
            try {
                taskStartTime = LocalDateTime.now();
                cacheResult = this.doTask();
                cacheUpdateTime = LocalDateTime.now();
                long seconds = LocalDateTimeUtils.twoTimeDuration(taskStartTime, cacheUpdateTime).getSeconds();

                if (seconds > restartTaskEval) {
                    log.warn(" websocket 任务执行时间超出循环时间 请注意！！！ ");
                }
            } catch (Exception e) {
                log.warn("websocket循环任务发生错误", e);
            } finally {
                taskStatus.set(0);
            }
            // 改为未执行状态

        }

    }

    public void setParam(Object... param) {
        this.param = param;
    }

    @Override
    public void run() {
        doAndCacheUpdate();
    }

    public abstract String doTask();

}
