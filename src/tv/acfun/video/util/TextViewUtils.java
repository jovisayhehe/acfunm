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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;

public class TextViewUtils {
    
    /**
     * 字符 转义字符
     *   “ &quot;
     *   & &amp; 
     *   < &lt;
     *   > &gt;
     *     &nbsp;
     */
    public static String getSource(String escapedHtml) {
        return escapedHtml.replaceAll("&quot;", "\"").replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">").replaceAll("&nbsp;", " ");
    }
    
    public static Drawable convertViewToDrawable(View view) {
      int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
      view.measure(spec, spec);
      view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
      Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
      Canvas c = new Canvas(b);
      c.translate(-view.getScrollX(), -view.getScrollY());
      view.draw(c);
      view.setDrawingCacheEnabled(true);
      Bitmap cacheBmp = view.getDrawingCache();
      Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
      view.destroyDrawingCache();
      return new BitmapDrawable(view.getResources(),viewBmp);

    }
}
