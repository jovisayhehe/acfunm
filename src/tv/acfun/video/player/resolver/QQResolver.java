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

import org.json.JSONArray;
import org.json.JSONObject;

import tv.acfun.video.player.MediaList;
import tv.acfun.video.player.MediaSegment;
import tv.acfun.video.player.ResolveException;
import android.content.Context;

/**
 * @author Yrom
 *
 */
public class QQResolver extends BaseResolver {
    
    public QQResolver(String vid){
        super(vid);
    }

    @Override
    public void resolve(Context context) throws ResolveException {
        try {
            String url = "http://vv.video.qq.com/geturl?otype=json&vid="+vid;
            
            String response = getResponseAsString(url);
            
            int start = "QZOutputJson=".length();
            int end = response.lastIndexOf(';');
            end = end < 0 ? response.length():end;
            JSONObject jsonObject = new JSONObject(response.substring(start,end));
            JSONArray viArray = jsonObject.getJSONObject("vd").getJSONArray("vi");
            for(int i=0;i<viArray.length();i++){
                JSONObject vi = viArray.getJSONObject(i);
                MediaSegment s = new MediaSegment();
                s.mDuration = (long)(Float.parseFloat(vi.getString("dur"))*1000); // "dur": "6022.36"
                s.mSize = vi.optLong("fs"); // "fs": 452279984
                s.mUrl = vi.getString("url");// "url": "http://vhotwsh.video.qq.com/flv/76/54/84sHlkSh6bE.mp4?vkey=...
                mList.add(s);
            }
            mHandler.sendEmptyMessage(ARG_OK);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResolveException(e);
        }
    }

    @Override
    public void resolveAsync(Context context) {
        new Thread(){
            public void run() {
                try{
                    resolve(null);
                }catch(ResolveException e){
                    mHandler.sendEmptyMessage(ARG_ERROR);
                }
            }
        }.start();
    }


    @Override
    public MediaList getMediaList(int resolution){
        // TODO Auto-generated method stub
        return null;
    }

}
