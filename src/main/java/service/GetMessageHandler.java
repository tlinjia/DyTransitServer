package service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.PacketConstructors;
import util.ParseResponse;

import java.util.Arrays;

import static app.DyTransitServer.task;
import static app.DyTransitServer.channels;

/**
 * Created by lin on 2016/11/9.
 */
public class GetMessageHandler extends ChannelInboundHandlerAdapter {
    final private Logger logger = LoggerFactory.getLogger(this.getClass());
    final private int roomId;
    final private int groupId;
    private boolean isFirstResponse = true;

    public GetMessageHandler(int roomId, int groupId) {
        this.roomId = roomId;
        this.groupId = groupId;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        task.setIncreased(false);
        ByteBuf buf = Unpooled.copiedBuffer(PacketConstructors.loginRequest(this.roomId));
        ChannelFuture future = ctx.writeAndFlush(buf);
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                logger.info("开始监听房间:" + roomId);
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        if (task.get(roomId)!=null && task.get(roomId).size() > 0) {
            if (!isFirstResponse && !ParseResponse.isErrorResponse(req)) {
                task.postMessage(roomId, req);
            } else if (isFirstResponse && ParseResponse.isLoginResponse(req)) {
                buf.writeBytes(PacketConstructors.joinGroupRequest(roomId, groupId));
                ChannelFuture future = ctx.writeAndFlush(buf);
                future.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        isFirstResponse = false;
                    }
                });
            } else if (isFirstResponse && !ParseResponse.isLoginResponse(req)) {
                for (ChannelId channelId : task.get(roomId)) {
                    channels.find(channelId).close();
                }
                logger.info("发送登录请求失败!，房间号:" + roomId);
                ctx.close();
            }else if(ParseResponse.isErrorResponse(req)){
                logger.info("与"+roomId+"连接中断，重新连接");
                GetMessage getMessage = new GetMessage(roomId);
                getMessage.connect();
                ctx.close();
            }
        } else {
            ctx.channel().close();
            logger.info(roomId + "连接已关闭!");
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("房间号:" + roomId + " 与斗鱼服务器连接异常:" + cause.getMessage());
        ctx.channel().close();
        try {
            if (task.get(roomId).size() > 0) {
                for (ChannelId channelId : task.get(roomId)) {
                    final Channel channel = channels.find(channelId);
                    ChannelFuture future = channel.writeAndFlush(Unpooled.copiedBuffer("请求失败!如确认房间号没有错误，可能是斗鱼服务器发生异常!".getBytes()));
                    future.addListener(new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            logger.info(channel.remoteAddress() + "已与服务器断开!");
                            channel.close();
                            ctx.close();
                        }
                    });
                }
            }
        } catch (Exception e) {

        }
    }
}
