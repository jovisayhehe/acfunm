package tv.acfun.video.util.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import tv.ac.fun.BuildConfig;
import tv.acfun.util.net.NetWorkUtil;
import tv.acfun.video.AcApp;
import tv.acfun.video.player.resolver.BaseResolver;
import tv.acfun.video.util.FileUtil;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * Segment File Download Task
 * @author Yrom
 *
 */
public class DownloadTask extends AsyncTask<Void, Integer, Boolean>{
    private static final int MAX_RETRIES = 3;
    private static final int MAX_REDIRECTS = 5;
    private static final int BUFFER_SIZE = 1 << 13;
    private int mId;
    private boolean isPaused;
    private DownloadInfo mInfo;
    private DownloadTaskListener mListener;
    private String TAG;
    
    public boolean isCancelled;
    public DownloadTask(DownloadInfo info){
        this(0,info);
    }
    public DownloadTask(int id, DownloadInfo info){
        this.mId = id;
        TAG = "DownloadTask - " + mId;
        this.mInfo = info;
    }
    
    public void pause(){
        isPaused = true;
        if(mListener!=null)
            mListener.onPause(this);
    }
    public void resume(){
        isPaused = false;
        if(mListener!=null)
            mListener.onResume(this);
        this.execute();
    }
    /**
     * shut down task, right now.
     */
    public void cancel(){
        isCancelled = true;
        if(mListener!=null)
            mListener.onCancel(this);
    }
    /** 获得Task的id，以便区分*/
    public int getId(){
        return mId;
    }
    private void initParams(HttpParams params){
        setTimeOut(params, 0);
        HttpConnectionParams.setSocketBufferSize(params, BUFFER_SIZE);
        HttpProtocolParams.setUserAgent(params, userAgent());
        HttpClientParams.setRedirecting(params, false); 
    }
    private void setTimeOut(HttpParams params, int retryTimes){
        HttpConnectionParams.setConnectionTimeout(params, retryTimes*2000+3000);
        HttpConnectionParams.setSoTimeout(params, retryTimes*2000 + 5000);
    }
    @Override
    protected Boolean doInBackground(Void... ps) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND); 
        State state = new State();
        state.mRequestUri = mInfo.url;
        HttpParams params = new BasicHttpParams();
        HttpClient client = new DefaultHttpClient();
        initParams(params);
        
        int finalStatus = 0;
        boolean finished = false;
        try{
            while(!finished && mInfo.retryTimes < MAX_RETRIES){
                if(BuildConfig.DEBUG) Log.i(TAG, (mInfo.retryTimes+1)+" 次尝试下载"+state.mRequestUri);
                
                HttpGet request = new HttpGet(state.mRequestUri);
                setTimeOut(params, mInfo.retryTimes);
                request.setParams(params);
                try{
                    executeDownload(client,request,state);
                    finished = true;
                    finalStatus = DownloadDB.STATUS_SUCCESS;
                } catch (RetryDownload e) {
                    if(state.mRequestUri.equals(mInfo.url)) // url 不同说明是重定向的，不计重试次数
                        mInfo.retryTimes++;
                    if(mListener!=null) 
                        mListener.onRetry(this);
                }finally{
                    request.abort();
                    request = null;
                }
            }
            return true;
        }catch (StopRequest e) {
            finalStatus = e.mFinalStatus;
            if(BuildConfig.DEBUG) Log.d(TAG, "status = "+finalStatus +","+e.getMessage());
            return false;
        }catch (Exception e) {
            finalStatus = 444; // 程序错误 = =
            Log.e(TAG, "download fail - url: "+state.mRequestUri,e);
            return false;
        }finally{
            if (client != null) {
                client.getConnectionManager().shutdown();
                client = null;
            }
            closeStream(state);
            ContentValues values = new ContentValues();
//           if(finalStatus == DownloadDB.STATUS_BAD_REQUEST && state.mSaveFile != null){
//                state.mSaveFile.delete();
//                state.mSaveFile = null;
//            }else 
//            去掉自动删除失败任务
            
            if(finalStatus == DownloadDB.STATUS_SUCCESS){
//                File newFile = new File(mInfo.savePath, mInfo.snum + FileUtil.getUrlExt(state.mRequestUri));
//                state.mSaveFile.renameTo(newFile);
//                重命名失败。。。
                values.put(DownloadDB.COLUMN_DATA, state.mSaveFile.getName());
            }
            mStatus = finalStatus;
            values.put(DownloadDB.COLUMN_STATUS, finalStatus);
            update(values);
            if(mListener != null)
                mListener.onCompleted(finalStatus, this);
        }
        
    }
    
    private int mStatus;
    
    public int getDownloadStatus(){
        return mStatus;
    }
    
    private void executeDownload(HttpClient client, HttpGet request, State state) throws RetryDownload, StopRequest {
        byte data[] = new byte[BUFFER_SIZE];
        
        setupDestinationFile(state);
        addRequestHeaders(state, request);
        if(mListener!=null) 
            mListener.onStart(this);
        if(state.mDownloadedBytes == state.mTotalBytes){
            if(BuildConfig.DEBUG) 
                Log.v(TAG, "skip already completed "+mInfo.vid +" - "+mInfo.snum);
            return ;
        }
        if(AcApp.getDownloadManager().isRequestWifi && !NetWorkUtil.isWifiConnected(AcApp.instance()))
            throw new StopRequest(DownloadDB.STATUS_QUEUED_FOR_WIFI, "WIFI unvailabe");
        HttpResponse response = sendRequest(state, client, request);
        handleExceptionalStatus(state, response);

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "received response for " + mInfo.url);
        }

        processResponseHeaders(state, response);
        InputStream entityStream = openResponseEntity(state, response);
        transferData(state, data, entityStream);
    }
    private void reportProgress(State state){
//        publishProgress(state.mDownloadedBytes);
        ContentValues values = new ContentValues();
        values.put(DownloadDB.COLUMN_CURRENT, state.mDownloadedBytes);
        values.put(DownloadDB.COLUMN_STATUS, DownloadDB.STATUS_RUNNING);
        update(values);
        
    }
    
    private void transferData(State state, byte[] data, InputStream entityStream) throws StopRequest, RetryDownload {
        for (;;) {
            int bytesRead = readFromResponse(state, data, entityStream);
            if (bytesRead == -1) { // success, end of stream already reached
                handleEndOfStream(state);
                return;
            }

            state.mGotData = true;
            writeDataToDestination(state, data, bytesRead);
            state.mDownloadedBytes += bytesRead;
            if(mListener != null)
                mListener.onProgress(bytesRead, this);
            reportProgress(state);
            if(isPaused)
                throw new StopRequest(DownloadDB.STATUS_PAUSED, "paused by user");
            if(isCancelled)
                throw new StopRequest(DownloadDB.STATUS_CANCELED, "CANCELED by user");
                
        }
        
    }

    private void writeDataToDestination(State state, byte[] data, int bytesRead) throws StopRequest {
        
        for (;;) {
            try {
                if(!AcApp.isExternalStorageAvailable()){
                    throw new StopRequest(600, "sdcard not availabe");
                }
                if (state.mStream == null) { // new download
                    state.mStream = new FileOutputStream(state.mSaveFile);
                }
                state.mStream.write(data, 0, bytesRead);
                return;
            } catch (IOException ex) {
                Log.i(TAG, "failed to write " + state.mSaveFile.getAbsolutePath(),ex);
            }
        }
        
        
    }

    private void handleEndOfStream(State state) throws StopRequest {
        ContentValues values = new ContentValues();
        values.put(DownloadDB.COLUMN_CURRENT, state.mDownloadedBytes);
        if (state.mHeaderContentLength == null) {
            values.put(DownloadDB.COLUMN_TOTAL, state.mDownloadedBytes);
        }
        update(values);

        boolean lengthMismatched = (state.mHeaderContentLength != null)
                && (state.mDownloadedBytes != Integer.parseInt(state.mHeaderContentLength));
        if (lengthMismatched) {
            if (cannotResume(state)) {
                throw new StopRequest(DownloadDB.STATUS_CANNOT_RESUME,
                        "mismatched content length");
            } else {
                throw new StopRequest(DownloadDB.STATUS_HTTP_DATA_ERROR,
                        "closed socket before end of file");
            }
        }
    }
    private boolean cannotResume(State state) {
        return state.mDownloadedBytes > 0 && state.mHeaderETag == null;
    }
    private int readFromResponse(State state, byte[] data, InputStream entityStream) throws StopRequest, RetryDownload {
        try {
            return entityStream.read(data);
        } catch (IOException ex) {
            ContentValues values = new ContentValues();
            values.put(DownloadDB.COLUMN_CURRENT, state.mDownloadedBytes);
            update(values);
            if (cannotResume(state)) {
                String message = "while reading response: " + ex.toString()+ 
                        ", can't resume interrupted download with no ETag";
                throw new StopRequest(DownloadDB.STATUS_CANNOT_RESUME,
                        message, ex);
            } else {
                throw new RetryDownload(ex);
            }
        }
    }
    private void update(ContentValues values){
        mInfo.manager.getProvider().update(mInfo.vid, mInfo.snum, values);
    }
    private InputStream openResponseEntity(State state, HttpResponse response) throws StopRequest {
        try {
            return response.getEntity().getContent();
        } catch (IOException ex) {
            throw new StopRequest(DownloadDB.STATUS_BAD_REQUEST,
                    "while getting entity: " + ex.toString(), ex);
        }
    }

    private void processResponseHeaders(State state, HttpResponse response) {
        if (state.mContinuingDownload)
            // ignore response headers on resume requests
            return;
        readResponseHeaders(state, response);
        initDB(state);
    }

    private void initDB(State state) {
        ContentValues values = new ContentValues();
        values.put(DownloadDB.COLUMN_TOTAL, state.mTotalBytes);
        values.put(DownloadDB.COLUMN_ETAG, state.mHeaderETag);
        values.put(DownloadDB.COLUMN_MIME, state.mMimeType);
        values.put(DownloadDB.COLUMN_UA, userAgent());
        values.put(DownloadDB.COLUMN_STATUS, DownloadDB.STATUS_RUNNING);
        if(TextUtils.isEmpty(mInfo.fileName))
            values.put(DownloadDB.COLUMN_DATA, state.mSaveFile.getName());
        update(values);
    }

    private void readResponseHeaders(State state, HttpResponse response) {
        Header header = response.getFirstHeader("Content-Location");
        if (header != null)
            state.mHeaderContentLocation = header.getValue();
        if (state.mMimeType == null) {
            header = response.getFirstHeader("Content-Type");
            if (header != null) {
                state.mMimeType = FileUtil.getMimeType(header.getValue());
            }
        }
        header = response.getFirstHeader("ETag");
        if (header != null) {
            state.mHeaderETag = header.getValue();
        }
        String headerTransferEncoding = null;
        header = response.getFirstHeader("Transfer-Encoding");
        if (header != null) {
            headerTransferEncoding = header.getValue();
        }
        if (headerTransferEncoding == null) {
            header = response.getFirstHeader("Content-Length");
            if (header != null) {
                state.mHeaderContentLength = header.getValue();
                state.mTotalBytes = Integer.parseInt(state.mHeaderContentLength);
                mInfo.totalBytes = state.mTotalBytes;
            }
        } else {
            Log.v(TAG, "ignoring content-length because of xfer-encoding");
        }
    }

    private void handleExceptionalStatus(State state, HttpResponse response) throws StopRequest, RetryDownload {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307) {
            handleRedirect(state, response, statusCode);
        }
        int expectedStatus = state.mContinuingDownload ? 206 : 200;
        if (statusCode != expectedStatus) {
            handleOtherStatus(state, statusCode);
        }
    }

    private void handleOtherStatus(State state, int statusCode) throws StopRequest {
        if (statusCode == 416) {
            // range request failed. it should never fail.
            throw new IllegalStateException("Http Range request failure: totalBytes = " +
                    state.mTotalBytes + ", downloadedBytes = " + state.mDownloadedBytes);
        }
        throw new StopRequest(statusCode, "http error " +
                statusCode + ", mContinuingDownload: " + state.mContinuingDownload);
    }

    private void handleRedirect(State state, HttpResponse response, int statusCode) throws StopRequest, RetryDownload {
        if (state.redirectCount >= MAX_REDIRECTS)
            throw new StopRequest(DownloadDB.STATUS_TOO_MANY_REDIRECTS,
                    "too many redirects");
        Header header = response.getFirstHeader("Location");
        if (header == null) return;
        String newUri;
        try {
            newUri = new URI(mInfo.url).resolve(new URI(header.getValue())).toString();
        } catch (URISyntaxException e) {
            throw new StopRequest(DownloadDB.STATUS_HTTP_DATA_ERROR,
                    "Couldn't resolve redirect URI");
        }
        ++ state.redirectCount;
        state.mRequestUri = newUri;
        if (statusCode == 301 || statusCode == 303) {
            // use the new URI for all future requests (should a retry/resume be necessary)
            state.mNewUri = newUri;
        }
        throw new RetryDownload();
    }

    private HttpResponse sendRequest(State state, HttpClient client, HttpGet request) throws StopRequest {
        try {
            return client.execute(request);
        }catch (IllegalArgumentException e) {
            throw new StopRequest(DownloadDB.STATUS_HTTP_DATA_ERROR, "try to execute request: "+e.toString());
        } catch (IOException ex) {
            throw new StopRequest(DownloadDB.STATUS_BAD_REQUEST,
                    "try to send request: " + ex.toString(), ex);
        }
        
    }

    private void addRequestHeaders(State state, HttpGet request) {
        if (state.mContinuingDownload) {
            if (state.mHeaderETag != null) {
                request.addHeader("If-Match", state.mHeaderETag);
            }
            request.addHeader("Range", "bytes=" + state.mDownloadedBytes + "-");
        }
    }
    private String luri;
    public String getLocalUri(){
        return luri;
        
    }
    private void setupDestinationFile(State state) throws StopRequest{
        if(state.mSaveFile != null) return;
        File path = null;
        if(TextUtils.isEmpty(mInfo.savePath))
            path = AcApp.getDownloadPath(mInfo.aid, mInfo.vid);
        else path = new File(mInfo.savePath);
        path.mkdirs(); // make dirs
        if(TextUtils.isEmpty(mInfo.url))
            throw new StopRequest(DownloadDB.STATUS_BAD_REQUEST, "found invalidate url");
        String fileName = mInfo.fileName;
        if(TextUtils.isEmpty(fileName)){ // new job
            fileName = mInfo.snum+FileUtil.getUrlExt(mInfo.url);
        }
        File f = new File(path, fileName);
        state.mSaveFile = f;
        luri = Uri.fromFile(f).toString();
        if(f.exists()){
            long fileLength = f.length();
            if (fileLength == 0) {
                f.delete();
            }else{
                try {
                    // append to it
                    state.mStream = new FileOutputStream(f,true);
                } catch (FileNotFoundException e) {
                    throw new StopRequest(DownloadDB.STATUS_BAD_REQUEST, "resume download fail: " + e.toString(), e);
                }
                state.mDownloadedBytes = (int) fileLength;
                if(mInfo.totalBytes != -1){
                    state.mHeaderContentLength = Long.toString(mInfo.totalBytes);
                    state.mTotalBytes = mInfo.totalBytes;
                }
                state.mHeaderETag = mInfo.etag;
                state.mContinuingDownload = true;
            }
        }
    }
    private void closeStream(State state) {
        try {
            if(state.mStream != null){
                state.mStream.close();
                state.mStream = null;
            }
        } catch (IOException e) {
        }
    }
    private String userAgent(){
        if(mInfo.userAgent == null)
            return BaseResolver.UA_DEFAULT;
        else 
            return mInfo.userAgent;
        
    }
    private static class State{
        public boolean mGotData;
        public File mSaveFile;
        public String mMimeType;
        /**
         * moved 
         */
        public String mNewUri;
        public String mRequestUri;
        public int redirectCount;
        public int mDownloadedBytes = 0;
        public int mTotalBytes = -1;
        public boolean mContinuingDownload = false;
        public String mHeaderETag;
        public String mHeaderContentLength;
        public String mHeaderContentLocation;
        public FileOutputStream mStream;
    }
    
    /**
     * retry
     */
    private class RetryDownload extends Exception{
        public RetryDownload(){
        }
        public RetryDownload(Throwable t) {
            super(t);
        }

        private static final long serialVersionUID = 228796856L;}
    /**
     * stop
     */
    private class StopRequest extends Exception {
        private static final long serialVersionUID = 621316701L;
        public int mFinalStatus;

        public StopRequest(int finalStatus, String message) {
            super(message);
            mFinalStatus = finalStatus;
        }

        public StopRequest(int finalStatus, String message, Throwable throwable) {
            super(message, throwable);
            mFinalStatus = finalStatus;
        }
    }
    public void setDownloadTaskListener(DownloadTaskListener l){
        mListener = l;
    }
    public int getTotalBytes(){
        return mInfo.totalBytes;
    }
    public interface DownloadTaskListener {
        void onStart(DownloadTask task);
        void onResume(DownloadTask task);
        void onProgress(int bytesRead, DownloadTask task);
        void onPause(DownloadTask task);
        void onCancel(DownloadTask task);
        void onRetry(DownloadTask task);
        void onCompleted(int status, DownloadTask task);
    }

}
