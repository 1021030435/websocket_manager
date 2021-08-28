package org.jeecg.websocket.task.node;

/**
 * @author: Zz Ai
 * @date: 2021-08-05 15:20
 **/

import java.io.IOException;
import java.time.LocalDateTime;

import org.jeecg.common.util.LocalDateTimeUtils;
import org.jeecg.websocket.manager.WebsocketConnect;
import org.jeecg.websocket.manager.WebsocketConnectManager;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

@Data
public class BaseTimeAllSend implements Runnable {

    @Override
    public void run() {
        JSONObject timeData = new JSONObject();
        timeData.put("socketName", "baseTime");

            timeData.put("data", LocalDateTimeUtils.formatTimeDefault(LocalDateTime.now()));
            // 发送
            WebsocketConnectManager.URL_WSCONNECT_CONCURRENTHASHMAP.values().stream().forEach(connect -> {
                for (WebsocketConnect websocketConnect : connect) {
                    try {

                        websocketConnect.sendMessage(timeData.toJSONString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            );

        }

}
