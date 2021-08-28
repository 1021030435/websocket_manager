package org.jeecg.websocket.task.node;

/**
 * @author: Zz Ai
 * @date: 2021-08-05 15:20
 **/

import org.jeecg.websocket.task.AbstractWebsocketTask;

import lombok.Data;

@Data
public class DemoTestTasknode extends AbstractWebsocketTask {

    @Override
    public String doTask() {

        return "DemoTest" + Thread.currentThread().getName();
    }
}
