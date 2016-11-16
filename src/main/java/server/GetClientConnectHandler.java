package server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.GetMessage;

import static app.DyTransitServer.task;
import static app.DyTransitServer.channels;

/**
 * Created by lin on 2016/11/11.
 */
public class GetClientConnectHandler extends ChannelInboundHandlerAdapter {
    final private Logger logger = LoggerFactory.getLogger(this.getClass());
    public static Object lock = new Object();
    private int roomId;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(ctx.channel().remoteAddress().toString() + "已连接");
        channels.add(ctx.channel());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] temp = new byte[buf.readableBytes()];
        buf.readBytes(temp);
        String message = new String(temp, "utf-8");
        if (message.contains("RoomId=")) {
            this.roomId = Integer.parseInt(message.substring(7));
            if(task.containsKey(roomId) && roomId != 0){
                logger.info(ctx.channel().remoteAddress()+"加入任务列表："+roomId);
                task.put(roomId,ctx.channel().id());
            }else{
                logger.info(ctx.channel().remoteAddress()+"请求监听"+roomId+"，新建连接");
                task.put(roomId,ctx.channel().id());
                GetMessage getMessage = new GetMessage(roomId);
                getMessage.connect();
//                synchronized (lock) {
//                    task.setIncreased(true);
//                    task.setRequestId(roomId);
//                }

            }
        }else if (message.contains("goodbye")){
            task.remove(roomId,ctx.channel().id());
            logger.info(ctx.channel().remoteAddress()+"已断开");
            ctx.close();
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info(ctx.channel().remoteAddress().toString() + ":" + cause.getMessage());
        task.remove(roomId,ctx.channel().id());
        ctx.close();
    }
}
