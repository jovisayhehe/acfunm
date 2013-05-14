package tv.avfun.util.download;

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

import tv.avfun.BuildConfig;
import tv.avfun.api.net.UserAgent;
import tv.avfun.app.AcApp;
import tv.avfun.util.FileUtil;
import tv.avfun.util.NetWorkUtil;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * Segment File Download Task(TODO)
 * @author Yrom
 *
 */
public class DownloadTask extends AsyncTask<Void, Integer, Boolean>{
    private static final String TAG = "DownloadTask";
    private static final String DOWNLOADING_FILE_EXT = ".av";
    private static final int MAX_RETRIES = 3;
    private static final int MAX_REDIRECTS = 5;
    private static final int BUFFER_SIZE = 1 << 13;
    
    private boolean isCompleted;
    private DownloadInfo mInfo;
    
    public DownloadTask(DownloadInfo info){
        this.mInfo = info;
    }
    
    //TODO pause task
    public void pause(){
        
    }
    //TODO resume task
    public void resume(){
        
    }
    /**
     * shut down task, right now.
     */
    public void cancel(){
        
    }
    @Override
    protected Boolean doInBackground(Void... ps) {
        State state = new State();
        HttpParams params = new BasicHttpParams();
        HttpClient client = new DefaultHttpClient();
        
        HttpConnectionParams.setConnectionTimeout(params, 3000);
        HttpConnectionParams.setSoTimeout(params, 5000);
        HttpConnectionParams.setSocketBufferSize(params, BUFFER_SIZE);
        HttpProtocolParams.setUserAgent(params, userAgent());
        HttpClientParams.setRedirecting(params, false); 
        int finalStatus = 0;
        try{
            while(mInfo.retryTimes < MAX_RETRIES){
                if(BuildConfig.DEBUG) Log.i(TAG, mInfo.retryTimes+"次尝试下载"+mInfo.url);
                HttpGet request = new HttpGet(mInfo.url);
                if(mInfo.retryTimes == 1)
                    HttpConnectionParams.setConnectionTimeout(params, 5000);
                request.setParams(params);
                try{
                    executeDownload(client,request,state);
                    mInfo.retryTimes = MAX_RETRIES;
                } catch (RetryDownload e) {
                    mInfo.retryTimes++;
                }finally{
                    request.abort();
                    request = null;
                }
            }
            return true;
        }catch (StopRequestException e) {
            // TODO: handle exception
            finalStatus = e.mFinalStatus;
            return false;
        }finally{
            if (client != null) {
                client.getConnectionManager().shutdown();
                client = null;
            }
            closeStream(state);
            if(finalStatus >= DownloadDB.STATUS_BAD_REQUEST){
                state.mSavePath.delete();
                state.mSavePath = null;
            }
        }
        
    }
    
    private void executeDownload(HttpClient client, HttpGet request, State state) throws RetryDownload, StopRequestException {
        byte data[] = new byte[BUFFER_SIZE];
        
        setupDestinationFile(state);
        addRequestHeaders(state, request);
        if(state.mDownloadedBytes == state.mTotalBytes){
            if(BuildConfig.DEBUG) 
                Log.i(TAG, "skip already completed "+mInfo.vid +" - "+mInfo.snum);
            return ;
        }
        if(!NetWorkUtil.isWifiConnected(AcApp.context()))
            throw new StopRequestException(DownloadDB.STATUS_QUEUED_FOR_WIFI, "WIFI unvailabe");
        HttpResponse response = sendRequest(state, client, request);
        handleExceptionalStatus(state, response);

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "received response for " + mInfo.url);
        }

        processResponseHeaders(state, response);
        InputStream entityStream = openResponseEntity(state, response);
        transferData(state, data, entityStream);
    }
    
    private void transferData(State state, byte[] data, InputStream entityStream) {
        // TODO transferData
        
    }

    private InputStream openResponseEntity(State state, HttpResponse response) {
        // TODO openResponseEntity
        return null;
    }

    private void processResponseHeaders(State state, HttpResponse response) {
        if (state.mContinuingDownload)
            // ignore response headers on resume requests
            return;
        readResponseHeaders(state, response);
        updateDatabase(state);
    }

    private void updateDatabase(State state) {
        ContentValues values = new ContentValues();
        values.put(DownloadDB.COLUMN_TOTAL, state.mTotalBytes);
        values.put(DownloadDB.COLUMN_ETAG, state.mHeaderETag);
        values.put(DownloadDB.COLUMN_MIME, state.mMimeType);
        mInfo.manager.getProvider().update(mInfo.vid, mInfo.snum, values );
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
                state.mTotalBytes = mInfo.totalBytes =
                        Integer.parseInt(state.mHeaderContentLength);
            }
        } else {
            Log.v(TAG, "ignoring content-length because of xfer-encoding");
        }
    }

    private void handleExceptionalStatus(State state, HttpResponse response) throws StopRequestException, RetryDownload {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307) {
            handleRedirect(state, response, statusCode);
        }
        int expectedStatus = state.mContinuingDownload ? 206 : 200;
        if (statusCode != expectedStatus) {
            handleOtherStatus(state, statusCode);
        }
    }

    private void handleOtherStatus(State state, int statusCode) throws StopRequestException {
        if (statusCode == 416) {
            // range request failed. it should never fail.
            throw new IllegalStateException("Http Range request failure: totalBytes = " +
                    state.mTotalBytes + ", downloadedBytes = " + state.mDownloadedBytes);
        }
        throw new StopRequestException(statusCode, "http error " +
                statusCode + ", mContinuingDownload: " + state.mContinuingDownload);
    }

    private void handleRedirect(State state, HttpResponse response, int statusCode) throws StopRequestException, RetryDownload {
        if (state.redirectCount >= MAX_REDIRECTS)
            throw new StopRequestException(DownloadDB.STATUS_TOO_MANY_REDIRECTS,
                    "too many redirects");
        Header header = response.getFirstHeader("Location");
        if (header == null) return;
        String newUri;
        try {
            newUri = new URI(mInfo.url).resolve(new URI(header.getValue())).toString();
        } catch (URISyntaxException e) {
            throw new StopRequestException(DownloadDB.STATUS_HTTP_DATA_ERROR,
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

    private HttpResponse sendRequest(State state, HttpClient client, HttpGet request) throws StopRequestException {
        try {
            return client.execute(request);
        }catch (IllegalArgumentException e) {
            throw new StopRequestException(DownloadDB.STATUS_HTTP_DATA_ERROR, "try to execute request: "+e.toString());
        } catch (IOException ex) {
            throw new StopRequestException(DownloadDB.STATUS_BAD_REQUEST,
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
    
    private void setupDestinationFile(State state) throws StopRequestException{
        File path = null;
        if(TextUtils.isEmpty(mInfo.savePath))
            path = AcApp.getDownloadPath(mInfo.aid, mInfo.vid);
        if(TextUtils.isEmpty(mInfo.url))
            throw new StopRequestException(DownloadDB.STATUS_BAD_REQUEST, "found invalidate url");
        String fileName = mInfo.snum+DOWNLOADING_FILE_EXT;
        File f = new File(path, fileName);
        state.mSavePath = f;
        if(f.exists()){
            long fileLength = f.length();
            if (fileLength == 0) {
                f.delete();
            }else{
                try {
                    // append to it
                    state.mStream = new FileOutputStream(f,true);
                } catch (FileNotFoundException e) {
                    throw new StopRequestException(DownloadDB.STATUS_BAD_REQUEST, "resume download fail: " + e.toString(), e);
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
            return UserAgent.MY_UA;
        else 
            return mInfo.userAgent;
        
    }
    private static class State{
        public File mSavePath;
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
        private static final long serialVersionUID = 228796856L;}
    /**
     * stop
     */
    private class StopRequestException extends Exception {
        private static final long serialVersionUID = 621316701L;
        public int mFinalStatus;

        public StopRequestException(int finalStatus, String message) {
            super(message);
            mFinalStatus = finalStatus;
        }

        public StopRequestException(int finalStatus, String message, Throwable throwable) {
            super(message, throwable);
            mFinalStatus = finalStatus;
        }
    }
   
}
