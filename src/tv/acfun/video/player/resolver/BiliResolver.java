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

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import tv.acfun.video.player.MediaList;
import tv.acfun.video.player.ResolveException;
import android.content.Context;

/**
 * @author Yrom
 *
 */
public class BiliResolver extends BaseResolver{
   
    public BiliResolver(String vid) {
        super(vid);
    }

    @Override
    public void resolve(Context context) throws ResolveException {
        String url = "http://interface.bilibili.tv/playurl?cid="+vid;
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
    public void resolveAsync(final Context context) {
        new Thread(){
            public void run() {
                try {
                    resolve(context);
                } catch (ResolveException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(ARG_ERROR);
                }
            }
        }.start();
    }

    @Override
    public MediaList getMediaList(int resolution) {
        return null;
    }
    
}
