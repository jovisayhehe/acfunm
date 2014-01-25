/*
 * Copyright (C) 2014 YROM.NET
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

package tv.acfun.video.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import tv.acfun.video.player.MediaList;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.DanmakuFactory;
import master.flame.danmaku.danmaku.util.IOUtils;
import android.graphics.Color;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * @author Yrom
 *
 */
public class DanmakuParser extends BaseDanmakuParser {
    private JSONArray mDanmakuJsonArray;
    public DanmakuParser(String danmakus){
        init(danmakus);
    }


    private void init(String danmakus) {
        mDanmakuJsonArray = JSON.parseArray(danmakus);
    }
    
    
    public DanmakuParser(File file) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            String danmakus = IOUtils.getString(in);
            init(danmakus);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
        
    }


    @Override
    public Danmakus parse() {
        return _parse(mDanmakuJsonArray);
    }
    public int size(){
        return mSize;
    }
    
    private int mSize = 0;
    private Danmakus _parse(JSONArray jsonArray) {
        Danmakus danmakus = null;
        if (jsonArray != null && jsonArray.size() > 0)
            danmakus = new Danmakus();
        for (int i = 0; i < jsonArray.size(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                String c = obj.getString("c");
                String[] values = c.split(",");
                if (values.length > 0) {
                    int type = Integer.parseInt(values[2]); // 弹幕类型
                    if (type == 7)
                        // TODO : parse advance danmaku json
                        continue;
                    long time = (long) (Float.parseFloat(values[0]) * 1000); // 出现时间
                    int color = Integer.parseInt(values[1]) | 0xFF000000; // 颜色
                    float textSize = Float.parseFloat(values[3]); // 字体大小
                    BaseDanmaku item = DanmakuFactory.createDanmaku(type, mDispWidth/(mDispDensity - 0.9f));
                    if (item != null) {
                        item.time = time;
                        item.textSize = textSize * (mDispDensity - 0.5f);
                        item.textColor = color;
                        item.textShadowColor = color <= Color.BLACK ? Color.WHITE : Color.BLACK;
                        DanmakuFactory.fillText(item, obj.getString("m"));
                        item.index = i;
                        item.setTimer(mTimer);
                        danmakus.addItem(item);
                    }
                }

            } catch (JSONException e) {
            }
        }
        mSize = danmakus.size();
        return danmakus;
    }


}
