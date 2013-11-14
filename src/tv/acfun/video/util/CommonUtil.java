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

package tv.acfun.video.util;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * @author Yrom
 * 
 */
public class CommonUtil {
    public static final int INVALID_POSITION = -1;
    private static String sExternalFilesDir;

    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static String getExternalFilesDir(Context context) {
        if (sExternalFilesDir == null)
            sExternalFilesDir = context.getExternalFilesDir(null).getAbsolutePath();
        return sExternalFilesDir;
    }
    
    public static File getExternalCacheDir(Context context, String type) {
        File cacheDir = new File(context.getExternalCacheDir(), type);
        cacheDir.mkdirs();
        return cacheDir;
    }
    
    public static int calculatePixelsFromDips(int dip, DisplayMetrics metrics){
        return TypedValue.complexToDimensionPixelOffset(dip, metrics);
    }
}
