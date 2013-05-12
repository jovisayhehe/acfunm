package tv.avfun.api.net;

import java.lang.reflect.Field;
import java.util.Random;

public final class UserAgent {

    public static final String IPAD      = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X; en-us) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3";
    public static final String Android_4 = "Mozilla/5.0 (Linux; U; Android 4.0.1; zh-cn) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    public static final String CHROME_26 = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31";
    public static final String IE_9      = "Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1)";
    public static final String DEFAULT   = CHROME_26;
    private static Random random;
    static{
        random = new Random();
    }
    public static String getRandom(){
        Field[] fields = UserAgent.class.getFields();
        int index= random.nextInt(fields.length);
        try {
            return fields[index].get(null).toString();
        } catch (Exception e) {
            return DEFAULT;
        }
    }
    
}
