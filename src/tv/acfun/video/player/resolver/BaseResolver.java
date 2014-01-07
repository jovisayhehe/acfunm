/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video.player.resolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tv.acfun.video.BuildConfig;
import tv.acfun.video.player.MediaList;
import tv.acfun.video.player.MediaList.OnResolvedListener;
import tv.acfun.video.player.MediaList.Resolver;
import tv.acfun.video.player.MediaSegment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Yrom
 *
 */
public abstract class BaseResolver implements Resolver, Callback {
    public static final String UA_DEFAULT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36"; 
    public static final int RESOLUTION_HD3 = 3;
    public static final int RESOLUTION_HD2 = 2;
    public static final int RESOLUTION_HD = 1;
    public static final int RESOLUTION_NORMAL = 0;
    public static final int RESOLUTION_DEFAULT = 1;
    protected static final int ARG_OK = 1;
    protected static final int ARG_ERROR = 0;
    protected static final String TAG = "Resolver";
    
    protected String vid;
    protected MediaList mList;
    protected Handler mHandler;
    // TODO: change parsing mode

    protected int mResolutionMode = RESOLUTION_DEFAULT;
    /**
     * @param resolution  {@code RESOLUTION_*}
     */
    public void setResolution(int resolution){
        Log.d(TAG, "RESOLUTION::"+resolution);
        mResolutionMode = resolution;
    }
    static {
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
    }
    
    private OnResolvedListener mOnresolvedListener;
    public BaseResolver(String vid){
        this.vid = vid;
        this.mList = new MediaList();
        mHandler = new Handler(this);
    }
    @Override
    public void clearCache() {
        // TODO Auto-generated method stub
    }

    public MediaList getMediaList(){
        return mList;
    }

    @Override
    public MediaSegment getMediaSegment(int segmentId) {
        return mList.get(segmentId);
    }

    @Override
    public void setOnResolvedListener(OnResolvedListener l) {
        mOnresolvedListener = l;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what == ARG_ERROR)
            mList = null;
        if (mOnresolvedListener != null)
            mOnresolvedListener.onResolved(this);
        
        return false;
    }
    
    public static String getResponseAsString(String url) {
        try {
            InputStream in = getResponseAsStream(url);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[2048];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            String res = new String(out.toByteArray());
            return res;
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.w(TAG, "获取数据失败" + "\n" + e.getMessage());
        }
        return null;
    }
    public static InputStream getResponseAsStream(String url) throws MalformedURLException, IOException{
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", UA_DEFAULT);
        conn.setConnectTimeout(6000);
        if (conn.getResponseCode() != 200)
            return null;
        InputStream in = conn.getInputStream();
        return in;
    }
    
    /**
     * 获得Jsondu对象
     * 
     * @param url
     * @return 获取失败返回null
     */
    public static JSONObject getJSONObject(String url) throws JSONException {
        String json = getResponseAsString(url);
        if (json != null)
            return new JSONObject(json);
        else
            return null;
    }
    
    /*
     * <video>
            <result>suee</result>
            <timelength>289863</timelength>
            <stream><![CDATA[letv]]></stream>
            <args><![CDATA[]]></args>
            <src>0</src>
            <durl>
                <order>1</order>
                <length>289863</length>
                <size>36124273</size>
                <chunks>565</chunks>
                <chunksize>64000</chunksize>
                <url><![CDATA[http://sh-cc2.acgvideo.com/8/7b/1273009-1.flv]]></url>
            </durl>
        </video>
     */
    /**
     * 
     * xml content handler
     * for bili and sina
     */
    protected class UrlContentHandler extends DefaultHandler {
        MediaSegment segment = null;
        int currentState;
        final int ORDER = 1;
        final int LENGTH = 2;
        final int URL = 5;
        
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String tagName = TextUtils.isEmpty(localName)?qName:localName;
            
            if(tagName.equals("durl")){
                segment = new MediaSegment();
            }else if(tagName.equals("order")){
                currentState = ORDER;
            }else if(tagName.equals("length")){
                currentState = LENGTH;
            }else if(tagName.equals("url")){
                currentState = URL;
            }else{
                currentState = 0;
            }
        }
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(localName.equals("durl")){
                mList.add(segment);
                segment = null;
            }
        }
        @Override
        public void endDocument() throws SAXException {
            mHandler.sendEmptyMessage(ARG_OK);
        }
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String theString = new String(ch, start, length);
            switch (currentState) {
            case LENGTH:
                segment.mDuration = Long.parseLong(theString);
                break;
            case URL:
                segment.mUrl = theString;
                break;
            default:
                break;
            }
            currentState = 0;
        }
        
    }
}
