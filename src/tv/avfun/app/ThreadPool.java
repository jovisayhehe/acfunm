package tv.avfun.app;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * 线程池
 */
public class ThreadPool {

    private static final String TAG     = ThreadPool.class.getSimpleName();
    private AtomicBoolean       bStoped = new AtomicBoolean(Boolean.FALSE);
    private ThreadPoolExecutor  mThreadQueue;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public ThreadPool(int maxSize) {
        if (Build.VERSION.SDK_INT > 8) {
            mThreadQueue = new ThreadPoolExecutor(maxSize<5?maxSize:maxSize, maxSize, 60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(), sThreadFactory);
            mThreadQueue.allowCoreThreadTimeOut(true);
        } else {
            mThreadQueue = new ThreadPoolExecutor(2, maxSize, 60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(), sThreadFactory);
        }
    }

    /**
     * 提交任务给线程池
     * 
     * @param run
     */
    public void post(Runnable run) {
        mThreadQueue.execute(run);
    }

    /**
     * 立即停止执行
     */
    public void stop() {
        if (!bStoped.get()) {
            mThreadQueue.shutdownNow();
            bStoped.set(Boolean.TRUE);
        }
    }

    private static final ThreadFactory sThreadFactory = new MySimpleThreadFactory();

    /**
     * 线程创建工厂
     */
    static class MySimpleThreadFactory implements ThreadFactory {

        /**
         * 保证线程池计数的原子性
         */
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, TAG + " - " + mCount.getAndIncrement());
        }
    }
}
