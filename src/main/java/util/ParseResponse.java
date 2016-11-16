package util;

/**
 * Created by lin on 2016/11/10.
 */
public class ParseResponse {

    public static boolean isLoginResponse(byte[] packet) {
        if (packet.length <= 12) {    //只有消息头
            return false;
        }
        String data = new String(packet, 12, packet.length - 12);
        if(data.contains("type@=loginres")){
            return true;
        }
        return false;
    }

    public static boolean isErrorResponse(byte[] packet){
        if (packet.length <= 12) {    //只有消息头
            return false;
        }
        String data = new String(packet, 12, packet.length - 12);
        if(data.contains("type@=error")){
            return true;
        }
        return false;
    }

}
