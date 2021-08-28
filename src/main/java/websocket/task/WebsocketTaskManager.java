package org.jeecg.websocket.task;

/**
 * @author: Zz Ai
 * @date: 2021-08-05 15:20
 **/

import java.util.concurrent.*;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jeecg.websocket.task.node.BaseTimeAllSend;
import org.jeecg.websocket.task.node.HeartBeatTask;
import org.jeecg.websocket.task.node.WebsocketTaskLoopTask;

import lombok.Data;

@Data
public class WebsocketTaskManager {
    private static final SynchronousQueue<Runnable> workQueue = new SynchronousQueue<>();

    public static final ThreadPoolExecutor WEBSOCKET_TASK_THREADPOOL =
        new ThreadPoolExecutor(1, 200, 30, TimeUnit.SECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
    // org.apache.commons.lang3.concurrent.BasicThreadFactory 有oom风险 谨慎使用 此处仅做轻量计算 并且不添加多于的任务
    public static final ScheduledExecutorService SCHEDULED_THREAD_POOL_EXECUTOR =
        new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder()
            .namingPattern("websocket-dispacher-task-schedule-pool-%d").daemon(true).build());

    // 会在初始化执行
    static {
        // 单线程循环进行任务分发或者消息推送
        // WEBSOCKET_TASK_THREADPOOL.execute(new HeartBeatTask());
        //
        // WEBSOCKET_TASK_THREADPOOL.execute(new BaseTimeAllSend());
        //
        // WEBSOCKET_TASK_THREADPOOL.execute(new WebsocketTaskLoopTask());

        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(new BaseTimeAllSend(), 0, 1, TimeUnit.SECONDS);

        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(new WebsocketTaskLoopTask(), 0, 1, TimeUnit.SECONDS);

        // 每10S检查一次
        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleWithFixedDelay(new HeartBeatTask(), 0, 10, TimeUnit.SECONDS);

    }

}
