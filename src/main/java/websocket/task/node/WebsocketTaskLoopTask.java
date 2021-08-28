package org.jeecg.websocket.task.node;

/**
 * @author: Zz Ai
 * @date: 2021-08-05 15:20
 **/

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jeecg.common.util.LocalDateTimeUtils;
import org.jeecg.websocket.manager.WebsocketConnect;
import org.jeecg.websocket.manager.WebsocketConnectManager;
import org.jeecg.websocket.task.AbstractWebsocketTask;
import org.jeecg.websocket.task.WebsocketTaskManager;

import lombok.Data;

@Data
public class WebsocketTaskLoopTask implements Runnable {

    @Override
    public void run() {

        ConcurrentHashMap<String, CopyOnWriteArrayList<AbstractWebsocketTask>> urlTaskConcurrenthashmap =
            WebsocketConnectManager.URL_TASK_CONCURRENTHASHMAP;
        ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketConnect>> urlWsconnectConcurrenthashmap =
            WebsocketConnectManager.URL_WSCONNECT_CONCURRENTHASHMAP;
        if (urlTaskConcurrenthashmap.isEmpty()) {
            return;
        }

        urlTaskConcurrenthashmap.forEach((k, v) -> {
            if (!urlWsconnectConcurrenthashmap.containsKey(k)) {
                urlTaskConcurrenthashmap.remove(k);
            }

            // 循环执行并且发送

            for (AbstractWebsocketTask task : v) {
                // 可能由于全部关闭连接 但是任务还没全关闭
                if (task.getConnectList().isEmpty()) {

                    v.remove(task);
                }

                // 保证任务的唯一性 即保证任务下的socket会定时接收数据
                WebsocketTaskManager.WEBSOCKET_TASK_THREADPOOL.execute(() -> {
                    // 检查是否过期
                    long intervalSeconds =
                        LocalDateTimeUtils.twoTimeDuration(task.getCacheUpdateTime(), LocalDateTime.now()).getSeconds();
                    if (intervalSeconds >= task.getRestartTaskEval()) {
                        // 先更新然后再发送
                        task.doAndCacheUpdate();
                        WebsocketConnectManager.doTaskAndSendTaskCache(task);
                    }

                });

            }

        });
    }
}
