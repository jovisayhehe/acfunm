package tv.avfun.app;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;

import com.umeng.analytics.MobclickAgent;

import tv.avfun.BuildConfig;
import tv.avfun.MainActivity;
import tv.avfun.util.DataStore;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

/**
 * 未抓捕的异常处理器。保存log
 * 
 * @author Yrom
 * 
 */
public class CrashExceptionHandler implements UncaughtExceptionHandler {

    private static final String TAG = "Crashed";
    private static final CrashExceptionHandler instance = new CrashExceptionHandler();
    private UncaughtExceptionHandler defHandler; 
    private CrashExceptionHandler() {
        defHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    public static CrashExceptionHandler instance(){
        return instance;
    }
    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        String logDir = AcApp.getLogsDir();
        String logFile = logDir + "/" + AcApp.getCurDateTime() + "_crashed.log";
        StringWriter sw = new StringWriter();
        try {
            PrintWriter pw = new PrintWriter(sw);
            pw.append("\n==============================\n");
            pw.append(AcApp.getCurDateTime());
            pw.append("\n==============================\n");
            // 获取手机的版本信息
            pw.append("VERSION = " + Build.VERSION.SDK_INT + "\n");
            pw.append("DENISTY = "+MainActivity.width +"x"+MainActivity.height+"\n");
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String key = field.getName();
                String value = field.get(null).toString();
                pw.append((key + " = " + value + "\n"));
                pw.flush();
            }
            pw.append("\n===============================\n");
            ex.printStackTrace(pw);
           
            pw.close();
            // DataStore.writeToFile(logFile,sw.toString());
            // 去掉本地记录log
            
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
        if (BuildConfig.DEBUG)
            Log.i(TAG, "哦喽！崩溃了"+ ex.toString()+ "\n"+ logFile );
        // Debug 模式下不发送
        else MobclickAgent.reportError(AcApp.context(),sw.toString());
        new Thread() {

            public void run() {
                Looper.prepare();
                AcApp.showToast("( ⊙ o ⊙ )  ‎不用力就不会挂，为什么就是不明白！");
                Looper.loop();
            }
        }.start();
        new Thread() {

            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
                if(defHandler != null)
                    defHandler.uncaughtException(thread, ex);
            }
        }.start();
    }

}
