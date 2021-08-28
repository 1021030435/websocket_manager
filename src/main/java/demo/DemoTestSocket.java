package org.jeecg.modules.dashboard.websocket.demo;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.jeecg.websocket.manager.WebsocketConnect;
import org.jeecg.websocket.task.AbstractWebsocketTask;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@ServerEndpoint(value = DemoTestSocket.SOCKET_URL)
@Component
@Slf4j
public class DemoTestSocket extends WebsocketConnect {

    public static final String SOCKET_URL = "/websocket/demo/{workshopCode}";

    // private static EquipmentDashboardService equipmentDashboardService;
    //
    // @Autowired
    // DemoTestSocket(EquipmentDashboardService equipmentDashboardService) {
    // this.equipmentDashboardService = equipmentDashboardService;
    // }
    @OnOpen
    public void onOpen(@PathParam(value = "workshopCode") String workshopCode, Session session) {
        super.connectRegister(SOCKET_URL, session, workshopCode);
        // 推进测试 任务
        DemoTestTask demoTestTask = new DemoTestTask(workshopCode);
        demoTestTask.setParam(workshopCode);
        pushTask(demoTestTask);
        // DemoTestTask demoTestTask2 = new DemoTestTask();
        // demoTestTask2.setParam(workshopCode);
        // pushTask(demoTestTask2);
        // DemoTest2Task demoTest2Task = new DemoTest2Task();
        // demoTest2Task.setParam(workshopCode);
        // pushTask(demoTest2Task);

    }

    private class DemoTestTask extends AbstractWebsocketTask {
        private final String workshopCode;

        DemoTestTask(String workshopCode) {
            this.workshopCode = workshopCode;
            super.restartTaskEval = 1;
        }

        @Override
        public String doTask() {
            // 此处只负责调用程序然后包装为前端所需要的格式 socketName：？？ 这种

            return "DemoTest" + Thread.currentThread().getName() + workshopCode;
        }
    }

    private class DemoTest2Task extends AbstractWebsocketTask {
        DemoTest2Task() {
            super.restartTaskEval = 10;
        }

        @Override
        public String doTask() {
            // 此处只负责调用程序然后包装为前端所需要的格式 socketName：？？ 这种

            return "DemoTest 我是10S推送" + Thread.currentThread().getName();
        }
    }

}
