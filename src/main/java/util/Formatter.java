package util;

/**
 * Created by lin on 2016/11/10.
 */
public class Formatter {
    public static byte[] toBytesLE(int n){ //little end
        byte [] b =  new   byte [ 4 ];
        b[0 ] = ( byte ) (n &  0xff );
        b[1 ] = ( byte ) (n >>  8  &  0xff );
        b[2 ] = ( byte ) (n >>  16  &  0xff );
        b[3 ] = ( byte ) (n >>  24  &  0xff );
        return  b;
    }
}
