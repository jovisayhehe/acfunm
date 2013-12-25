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

package tv.acfun.video.api;

/**
 * 
 * @see <a href="http://wiki.acfun.tv/index.php/APIDoc4APP">APIDoc4APP</a>
 * @author Yrom
 * 
 */
public class API {
    public static final String BASE_URL = "http://api.acfun.tv";
    public static final String HOME_CATS = BASE_URL + "/home/categories";
    public static final String VIDEO_DETAIL = BASE_URL + "/videos/%d";
    public static final String CHANNEL_CATS = BASE_URL + "/videocategories";
    public static final String VIDEO_LIST = BASE_URL + "/videos?class=%d&cursor=%d";
    public static final String EXTRAS_CHANNEL_ID = "extras_channel_id";
    public static final String EXTRAS_CHANNEL_NAME = "extras_channel_name";
    public static final String EXTRAS_CATEGORY_ID = "extras_category_id";
    public static final String EXTRAS_CATEGORY_IDS = "extras_category_ids";
    public static String getVideosUrl(int catId, int page, boolean isoriginal) {
        String url = String.format(API.VIDEO_LIST, catId, page*20);
        if (isoriginal) url = url + "&isoriginal=true";
        return url;
    }
}
