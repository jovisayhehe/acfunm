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

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import tv.acfun.video.player.MediaList;
import tv.acfun.video.player.ResolveException;
import android.content.Context;
import android.util.Log;

/**
 * @author Yrom
 * 
 */
public class SinaResolver extends BaseResolver {
    public SinaResolver(String sid) {
        super(sid);
    }

    @Override
    public void resolve(Context context) throws ResolveException {
        // TODO Auto-generated method stub
        String url = getContentUrl(vid);
        Log.i(TAG, "sina video url = " + url);
        try {
            InputStream stream = getResponseAsStream(url);
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            ContentHandler contentHandler = new UrlContentHandler();
            xmlReader.setContentHandler(contentHandler);
            xmlReader.parse(new InputSource(stream));
        } catch (Exception e) {
            throw new ResolveException(e);
        }
    }

    @Override
    public void resolveAsync(Context context) {
        // TODO Auto-generated method stub
        new Thread() {
            public void run() {
                try {
                    resolve(null);
                } catch (ResolveException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(ARG_ERROR);
                }
            }
        }.start();
    }

    private static String getContentUrl(String vid) {
        double d = Math.random();
        long millis = System.currentTimeMillis() / 64000;
        String key = getKey(vid + "Z6prk18aWxP278cVAH" + String.valueOf(millis) + String.valueOf(d));
        String key2 = key.subSequence(0, 16) + String.valueOf(millis);
        StringBuilder builder = new StringBuilder()
            .append("http://v.iask.com/v_play.php?vid=")
            .append(vid)
            .append("&r=video.sina.com.cn")
            .append("&referrer=http%3A%2F%2Fvideo.sina.com.cn%2F")
            .append("&v=4.1.42.33&p=i&k=")
            .append(key2)
            .append("&ran=")
            .append(d);
        return builder.toString();
    }

    private static String getKey(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(str.getBytes());
            byte[] bs = digest.digest();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < bs.length; i++) {
                Byte b = Byte.valueOf(bs[i]);
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public MediaList getMediaList(int resolution) {
        // TODO Auto-generated method stub
        return null;
    }
}
