package service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by lin on 2016/11/9.
 */
public class GetMessage {
    final static private String host = "openbarrage.douyutv.com";   //斗鱼弹幕服务器
    final static private int port = 8601;   //端口
    final private int groupId = -9999;  //弹幕分组号
    final int roomId;

    public GetMessage(int roomId) {
        this.roomId = roomId;
    }


    public void connect() {
        new Thread(new Runnable() {
            public void run() {
                EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
                try{
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                            .option(ChannelOption.TCP_NODELAY,true)
                            .handler(new ChannelInitializer<SocketChannel>(){
                                @Override
                                public void initChannel(SocketChannel socketChannel) throws Exception{socketChannel.pipeline().addLast(new GetMessageHandler(roomId,groupId));
                                }
                            });
                    //异步连接
                    ChannelFuture channelFuture = bootstrap.connect(host,port).sync();

                    //等待客户连接关闭
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    eventLoopGroup.shutdownGracefully();
                }
            }
        }).start();

    }
}
