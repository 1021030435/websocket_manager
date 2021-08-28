package org.jeecg.websocket.manager;

/**
 * @author: Zz Ai
 * @date: 2021-08-05 12:02
 **/

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import org.jeecg.websocket.task.AbstractWebsocketTask;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @description:
 *
 * @author: Zz Ai
 *
 * @create: 2021-08-05 12:02
 **/
@Slf4j
@Data
public abstract class WebsocketConnect {

    public Session session;
    public String url;
    public Object[] connectParam;

    public static final String PING = "ping";
    public LocalDateTime lastPingTime = LocalDateTime.now();
    public ArrayList<AbstractWebsocketTask> taskArrayList = new ArrayList<>();

    /**
     * websocket 发生错误
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        try {
            session.close();
            error.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        sendMessage(message);
        if (PING.equals(message)) {
            lastPingTime = LocalDateTime.now();
            sendMessage("pong");
        }

        // System.err.println(this);
    }

    /**
     * 发送消息 并发调用会报 The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method
     *
     * @param message
     */
    public synchronized void sendMessage(String message) throws IOException {
        try {
            if (this.session.isOpen()) {
                session.getBasicRemote().sendText(message);
            } else {
                // 下次修整可以换个高级点的打印
                System.err.println("session is closed");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @OnClose
    public void onClose() {
        // TODO: 移除此连接
        log.info("设备主看板主看板有一连接关闭！当前在线人数为");
    }

    public void pushTask(AbstractWebsocketTask task) {
        task.setUrl(url);
        WebsocketConnectManager.pushTaskAndRun(task, this);
    };

    public boolean connectRegister(String url, Session session, Object... param) {
        this.url = url;
        this.session = session;
        this.connectParam = param;
        // 添加进管理类
        WebsocketConnectManager.URL_WSCONNECT_CONCURRENTHASHMAP.compute(url, (k, v) -> {
            if (v == null) {
                v = new CopyOnWriteArrayList<WebsocketConnect>();
            }
            v.add(this);
            return v;
        });

        return true;
    };

    // public boolean taskRegister(String url, ) {
    // this.url = url;
    // this.session = session;
    // this.param = param;
    // return true;
    // };

}
