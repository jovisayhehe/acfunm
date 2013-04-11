package tv.avfun.util;

import android.os.Handler;
import android.os.Message;


/**
 * 简单的异步任务
 * @author Yrom
 *
 */
public abstract class MyAsyncTask {
    // 默认为执行成功
    private static int OK = 0;
    private static int SIGH =1;
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            boolean result = msg.what==OK;
            onPublishResult(result);
            if(result) postExecute();
        }
    };
    
    /**
     * 执行！
     */
    public void execute(){
        preExecute();
        new Thread(){
            public void run() {
                doInBackground();
            }
        }.start();
        
    }
    /**
     * 发布执行结果。重写{@link #onPublishResult(boolean)}来接收结果。
     * @param succeeded 成功与否
     */
    public void publishResult(boolean succeeded){
        mHandler.sendEmptyMessage(succeeded?OK:SIGH);
    }
    /**
     * 发布出去。运行在主线程
     * @param succeeded
     */
    public void onPublishResult(boolean succeeded) {
    }
    /**
     * 执行完毕。只有当{@link #publishResult(boolean)}说执行结果成功才会调用此方法。
     */
    public void postExecute() {
        
    }
    /**
     * 执行之前。
     */
    public void preExecute() {
        
    }
    /**
     * 任务在子线程中执行。别忘了调用{@link #execute()}
     */
    public abstract void doInBackground();
}
