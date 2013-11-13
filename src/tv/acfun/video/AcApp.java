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

package tv.acfun.video;

import tv.acfun.video.util.net.Connectivity;
import android.app.Application;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.toolbox.ImageLoader;

/**
 * @author Yrom
 *
 */
public class AcApp extends Application {
    private static AcApp sInstance;
    private static Context sContext;

    public void onCreate() {
        super.onCreate();
        sContext = sInstance = this;
        Connectivity.getGloableQueue(this);
    }
    public static AcApp instance() {
        return sInstance;
    }
    
    public static void addRequest(Request<?> request){
        Connectivity.addRequest(request);
    }
    
    public static void cancelAllRequest(RequestFilter filter){
        Connectivity.cancelAllRequest(filter);
    }
    
    public static void cancelAllRequest(Object tag){
        Connectivity.cancelAllRequest(tag);
    }
    
    public static ImageLoader getGloableLoader(){
        return Connectivity.getGloableLoader(sContext);
    }
    
    public static byte[] getDataInDiskCache(String key){
        return Connectivity.getDataInDiskCache(sContext, key);
    }
}
