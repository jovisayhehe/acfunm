package tv.avfun.util;

import android.os.Handler;
import android.os.Message;


/**
 * 简单的异步任务<br>
 * @author Yrom
 *
 */
public abstract class MyAsyncTask {
    // 默认为执行成功
    private static final int OK = 0;
    private static final int SIGH =1;
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            onPublishResult(msg.what==OK);
            onPostExecute();
        }
    };
    
    /**
     * 执行！
     * <p>1. {@link #onPreExecute()}<br>
     * 2. {@link #doInBackground()}<br>
     * 3. {@link #onPublishResult(boolean)}<br>
     * 4. {@link #onPostExecute()}</p>
     */
    public final void execute(){
        onPreExecute();
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
    public final void publishResult(boolean succeeded){
        mHandler.sendEmptyMessage(succeeded?OK:SIGH);
    }
    /**
     * 发布出去。运行在主线程
     * @param succeeded
     */
    protected void onPublishResult(boolean succeeded) {
    }
    /**
     * 执行完毕调用此方法。
     */
    protected void onPostExecute() {
        
    }
    /**
     * 执行之前。
     */
    protected void onPreExecute() {
        
    }
    /**
     * 任务在子线程中执行。别忘了调用{@link #execute()}
     */
    protected abstract void doInBackground();
}
