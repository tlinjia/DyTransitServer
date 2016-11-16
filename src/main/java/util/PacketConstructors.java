package util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 斗鱼协议：
 * 数据长度4字节
 * 数据长度4字节
 * 消息类型2字节，689标志发送给服务器的类型，690标志接收自服务器的数据
 * 加密字段1字节，默认0
 * 保留字段1字节，默认0
 * 数据部分，结尾加'\0'字符
 * Created by lin on 2016/11/10.
 */
public class PacketConstructors {

    public static byte[] loginRequest(int roomId){
        String loginReq = "type@=loginreq/roomid@="+roomId+"/\0";
        return packet(loginReq,689);
    }

    public static byte[] joinGroupRequest(int roomId,int groupId){
        String joinGroupReq = "type@=joingroup/rid@="+roomId+"/gid@="+groupId+"/\0";
        return packet(joinGroupReq,689);
    }

    public static byte[] privateMessage(String data){
        return packet(data,700);
    }

    public static byte[] packet(String data,int tag){
        byte[] packetLength = Formatter.toBytesLE(data.length() + 8);
        byte[] type = Formatter.toBytesLE(tag);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.write(packetLength,0,4);   //写入长度,占4个字节
            dataOutputStream.write(packetLength,0,4);   //再次写入长度,占4个字节
            dataOutputStream.write(type,0,2);   //写入消息类型689，占2个字节
            dataOutputStream.writeByte(0);  //写入加密字段，默认为0
            dataOutputStream.writeByte(0);  //写入保密字段，默认为0
            dataOutputStream.writeBytes(data);  //写入数据

        } catch (IOException e) {
            System.out.println("构造以下报文段发生错误:"+data);
            System.out.println(e.getMessage());
            return null;
        }
        return byteArrayOutputStream.toByteArray();
    }
}
