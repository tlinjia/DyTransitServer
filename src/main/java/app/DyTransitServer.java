package app;

import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.GetClientConnect;
import service.GetMessage;
import util.TaskList;

/**
 * Created by lin on 2016/11/14.
 */
public class DyTransitServer {
    final static private int port = 4747;  //接收连接的端口号
    public static final DefaultChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static final TaskList task = new TaskList();

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                try {
                    new GetClientConnect().bind(port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

//        new Thread(){
//            @Override
//            public void run() {
//                while (true){
//                    if(task.isIncreased()){
//                        new Thread(new GetMessage(task.getRequestId())).start();
//                        task.setIncreased(false);
//                    }
//
//                }
//            }
//        }.start();
    }
}
