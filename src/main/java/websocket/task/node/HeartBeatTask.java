package org.jeecg.websocket.task.node;

/**
 * @author: Zz Ai
 * @date: 2021-08-05 15:20
 **/

import org.jeecg.websocket.manager.WebsocketConnect;
import org.jeecg.websocket.manager.WebsocketConnectManager;

import lombok.Data;

@Data
public class HeartBeatTask implements Runnable {
    @Override
    public void run() {
        WebsocketConnectManager.URL_WSCONNECT_CONCURRENTHASHMAP.values().stream().forEach(connect -> {

                for (WebsocketConnect websocketConnect : connect) {
                    WebsocketConnectManager.checkPingPong(websocketConnect);

                }
        }

        );
    }

}
