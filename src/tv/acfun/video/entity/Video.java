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

package tv.acfun.video.entity;

import java.util.ArrayList;

/**
 * {
  "acId": 904616,
  "name": "【10月新番】苍蓝钢铁的琶音 06【白月】",
  "desc": "[wiki新番连载索引#C]",
  "previewurl": "http://static.acfun.tv/dotnet/artemis/u/cms/www/201311/12111743w68l.png",
  "viewernum": 1459,
  "collectnum": 0,
  "commentnum": 11,
  "createtime": 2013,
  "creator": {
    "id": 259574,
    "name": "伊藤惣太",
    "avatar": "http://static.acfun.tv/dotnet/artemis/u/cms/www/201310/19192311lcd5.jpg"
  },
  "isoriginal": 1,
  "tags": [
    "苍蓝钢铁战舰",
    "苍蓝钢铁的琶音"
  ],
  "category": {
    "id": 67,
    "name": ""
  },
  "episodes": [
    {
      "videoId": 779819,
      "sourceId": "119196848",
      "type": "sina",
      "commentId": "119196848"
    }
  ],
  "channelId": 67
}
 * @author Yrom
 *
 */
public class Video {
    public int acId;
    public int viewernum;
    public int collectnum;
    public int commentnum;
    
    public String name;
    public String desc;
    public String previewurl;
    
    public User creator;
    
    public boolean isoriginal;
    
    public String[] tags;
    
    public ArrayList<VideoPart> episodes;
    
    public int channelId;
    
    @Override
    public String toString() {
        return name+"("+acId+")";
    }
}
