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

package tv.acfun.video.util.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import master.flame.danmaku.danmaku.util.IOUtils;
import tv.acfun.util.net.Connectivity;
import tv.acfun.video.player.resolver.BaseResolver;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

/**
 * @author Yrom
 *
 */
public class DanmakusRequest extends StringRequest {
    private static final String TAG = DanmakusRequest.class.getSimpleName();
    private File mDmFile;
    private Listener<String> mListener;
    public DanmakusRequest(String cid, String savePath, Listener<String> listener, ErrorListener errorListener) {
        super(String.format("http://comment.acfun.tv/%s.json", cid), listener, errorListener);
        mListener = listener;
        if(savePath!= null)
            mDmFile = new File(savePath,cid+".json");
    }
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed = new String(response.data, Charset.defaultCharset());
        if(mDmFile != null){
            OutputStream out = null;
            try {
                out = new FileOutputStream(mDmFile);
                out.write(response.data);
                Log.i(TAG, "downloaded dm file::"+mDmFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                IOUtils.closeQuietly(out);
            }
        }
        return Response.success(parsed, Connectivity.newCache(response, 90));
    }
    @Override
    protected void deliverResponse(String response) {
        if(mListener != null) mListener.onResponse(response);
    }
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if (headers == null || headers.equals(Collections.EMPTY_MAP)) {
            headers = new HashMap<String, String>();
        }
        headers.put("User-Agent", BaseResolver.UA_DEFAULT);
        return headers;
    }
}
