package util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import java.util.HashMap;
import java.util.HashSet;

import static app.DyTransitServer.channels;

/**
 * Created by lin on 2016/11/11.
 */
public class TaskList {
    private HashMap<Integer,HashSet> map;
    volatile private int size = 0;
    volatile private boolean increased = false;
    volatile private int requestId;

    synchronized public int getRequestId() {
        return requestId;
    }

    synchronized public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public boolean isIncreased() {
        return increased;
    }

    synchronized public void setIncreased(boolean increased) {
        this.increased = increased;
    }

    public int getSize() {
        return size;
    }

    public TaskList(){
        map = new HashMap<Integer, HashSet>();
    }

    public void put(int key, ChannelId value){
        if (map.containsKey(key)){
            map.get(key).add(value);
        }else {
            HashSet<ChannelId> set = new HashSet<ChannelId>();
            set.add(value);
            map.put(key,set);
        }
        size++;
    }

    public boolean remove(int key,ChannelId value){
        boolean result = false;
        if(map.containsKey(key)){
            if(map.get(key).contains(value)){
                result = map.get(key).remove(value);
                if(map.get(key).size()<=0)
                    map.remove(key);
                if(result)
                    size--;
            }
        }
        return result;
    }

    public boolean containsKey(int key){
        return map.containsKey(key);
    }

    public HashSet<ChannelId> get(int key){
        return map.get(key);
    }

    public void postMessage(int key,byte[] message) throws InterruptedException {
        ByteBuf buf = Unpooled.copiedBuffer(message);
        for(ChannelId channelId : get(key)){
            Channel channel = channels.find(channelId);
           if(channel.isWritable()){
               channel.writeAndFlush(buf.retain());
           }
        }
    }
}
