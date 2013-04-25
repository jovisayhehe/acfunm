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
    private boolean result = true;
    private static final int OK = 0;
    private static final int GOT =1;
    private boolean shouldBeCancel = false;
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            if(!shouldBeCancel){  // 保证中途取消也不会被执行
                if(msg.what == GOT)
                    onPublishResult(result);
                else if(msg.what == OK)
                    onPostExecute();
            }
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
                if(!shouldBeCancel){
                    doInBackground();
                    mHandler.obtainMessage(OK).sendToTarget();
                }
            }
        }.start();
        
    }
    /**
     * 发布执行结果。重写{@link #onPublishResult(boolean)}来接收结果。
     * @param succeeded 成功与否
     */
    public final void publishResult(boolean succeeded){
        result = succeeded;
        mHandler.sendEmptyMessage(GOT);
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
     * 执行之前。可以取消任务{@link #cancel()}
     */
    protected void onPreExecute() {
        
    }
    /**
     * 任务在子线程中执行。别忘了调用{@link #execute()}
     */
    protected abstract void doInBackground();
    /**
     * 取消任务。取消后，{@link #doInBackground()}
     *  {@link #onPublishResult(boolean)}
     *   {@link #onPostExecute()}都会不会被执行
     */
    public final void cancel(){
        this.shouldBeCancel = true;
    }
}
