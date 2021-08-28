package org.jeecg.websocket.manager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.websocket.Session;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.LocalDateTimeUtils;
import org.jeecg.websocket.task.AbstractWebsocketTask;
import org.jeecg.websocket.task.WebsocketTaskManager;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 *
 * @author: Zz Ai
 *
 * @create: 2021-08-05 15:28
 **/
@Slf4j
public class WebsocketConnectManager {
    // 根据url整理的连接 结构拆分可以继续优化按照 session Id
    public static final ConcurrentHashMap<String,
        CopyOnWriteArrayList<WebsocketConnect>> URL_WSCONNECT_CONCURRENTHASHMAP = new ConcurrentHashMap<>();
    // 根据url整理的任务 内部任务按照url以及param进行比对 不会出现重复的任务 会定时处理任务并推送
    public static final ConcurrentHashMap<String,
        CopyOnWriteArrayList<AbstractWebsocketTask>> URL_TASK_CONCURRENTHASHMAP = new ConcurrentHashMap<>();
    public static final int OVERTIME = 30;

    public static Boolean checkPingPong(WebsocketConnect websocketConnect) {
        Session session = websocketConnect.getSession();
        if (!session.isOpen()) {
            removeConnect(websocketConnect);
            return Boolean.TRUE;
        }

        long seconds =
            LocalDateTimeUtils.twoTimeDuration(websocketConnect.lastPingTime, LocalDateTime.now()).getSeconds();

        boolean removeFlag = seconds > OVERTIME;

        if (removeFlag) {

            try {

                session.close();
                log.info("  url:" + websocketConnect.getUrl() + "  sessionId:" + session.getId() + "由于心跳超时已被关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 删除连接 包括url内的以及task内的
            removeConnect(websocketConnect);
        }
        return removeFlag;
    }

    private static void removeConnect(WebsocketConnect websocketConnect) {

        CopyOnWriteArrayList<AbstractWebsocketTask> abstractWebsocketTasks =
            URL_TASK_CONCURRENTHASHMAP.get(websocketConnect.getUrl());

        for (AbstractWebsocketTask abstractWebsocketTask : abstractWebsocketTasks) {
            CopyOnWriteArrayList<WebsocketConnect> connectList = abstractWebsocketTask.getConnectList();
            connectList.remove(websocketConnect);
        }

        CopyOnWriteArrayList<WebsocketConnect> websocketConnects =
            URL_WSCONNECT_CONCURRENTHASHMAP.get(websocketConnect.getUrl());
        Assert.notNull(websocketConnects, "should not be null");

        websocketConnects.remove(websocketConnect);

    }

    /**
     * 负责鉴定是否重复的任务 以及首次任务的结果推送
     *
     * @param task
     * @param websocketConnect
     */
    public static synchronized void pushTaskAndRun(AbstractWebsocketTask task, WebsocketConnect websocketConnect) {
        // 必须加锁 conpute会有并发风险
        URL_TASK_CONCURRENTHASHMAP.compute(task.getUrl(), (k, v) -> {
            if (v == null) {
                // conn关联task 并且发送
                CopyOnWriteArrayList<AbstractWebsocketTask> abstractWebsocketTasks = new CopyOnWriteArrayList<>();
                v = abstractWebsocketTasks;
                task.getConnectList().add(websocketConnect);
                v.add(task);
                doTaskAndSendTaskCache(task);

                return v;
            }

            // 开始进行比对
            for (AbstractWebsocketTask originalWebsocketTask : v) {
                Object[] param = originalWebsocketTask.getParam();
                Object[] taskParam = task.getParam();
                // 参数均不相等的时候 将任务添加
                Class<? extends AbstractWebsocketTask> taskClass = task.getClass();
                Class<? extends AbstractWebsocketTask> clazz = originalWebsocketTask.getClass();
                // 同一class类型的任务 以及相同参数才算 相同的task 此时只发送缓存
                // 直接返回V 结束循环
                if (Arrays.equals(param, taskParam) && clazz.equals(taskClass)) {
                    // 获取原始的唯一任务
                    originalWebsocketTask.getConnectList().add(websocketConnect);
                    // 此处单独发送 如果没有缓存结果的话会跳出 说明已有任务但是没结果肯定是在运行的
                    doTaskAndSendTaskCache(originalWebsocketTask, websocketConnect);
                    return v;
                }

            }
            // 并没有获得相同的task 类型以及参数相同
            task.getConnectList().add(websocketConnect);
            doTaskAndSendTaskCache(task);
            v.add(task);
            return v;
        });

    }

    /**
     * 发送此task所关联的任务数据
     *
     * @param task
     */
    public static void doTaskAndSendTaskCache(AbstractWebsocketTask task) {
        WebsocketTaskManager.WEBSOCKET_TASK_THREADPOOL.execute(() -> {
            // 关于多线程并发执行同一任务 解决方案：doAndCacheUpdate会进行原子化改变任务执行状态 保证唯一执行后在进行所有connect的发送结果

            if (StringUtils.isEmpty(task.getCacheResult())) {
                task.doAndCacheUpdate();
            }

            CopyOnWriteArrayList<WebsocketConnect> connectList = task.getConnectList();

            CopyOnWriteArrayList<WebsocketConnect> websocketConnects =
                URL_WSCONNECT_CONCURRENTHASHMAP.get(task.getUrl());

            if (CollectionUtils.isEmpty(websocketConnects)) {
                URL_TASK_CONCURRENTHASHMAP.remove(task.getUrl());
                return;
            }

            for (WebsocketConnect websocketConnect : connectList) {
                // 多线程发送 此处有并发问题 多连接同参数打进来会造成多次执行任务

                try {
                    if (StringUtils.isNotEmpty(task.getCacheResult())) {
                        websocketConnect.sendMessage(task.cacheResult);
                    }
                    // 捕获excetpiton 防止其余任务不进行推送
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 仅仅只对此一连接进行推送 并且当没有缓存时会执行任务
     *
     * @param task
     * @param connect
     */
    public static void doTaskAndSendTaskCache(AbstractWebsocketTask task, WebsocketConnect connect) {

        // 多线程发送
        WebsocketTaskManager.WEBSOCKET_TASK_THREADPOOL.execute(() -> {
            try {
                if (StringUtils.isBlank(task.cacheResult)) {
                    task.doAndCacheUpdate();
                }
                if (StringUtils.isNotEmpty(task.getCacheResult())) {
                    connect.sendMessage(task.cacheResult);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    // public static void main(String[] args) {
    // CopyOnWriteArrayList<String> a = new CopyOnWriteArrayList<String>();
    //
    // a.add("1");
    // a.add("2");
    // for (String s : a) {
    // a.remove("1");
    // a.add("3");
    // }
    // System.err.println(a);
    //
    // }

}
