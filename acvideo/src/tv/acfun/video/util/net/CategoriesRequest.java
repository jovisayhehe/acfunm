package tv.acfun.video.util.net;

import java.io.UnsupportedEncodingException;
import java.util.List;

import tv.acfun.util.net.Connectivity;
import tv.acfun.util.net.CustomUARequest;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Category;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class CategoriesRequest extends CustomUARequest<List<Category>> {

        public CategoriesRequest(Listener<List<Category>> listener, ErrorListener errorListner) {
            super(API.CHANNEL_CATS, null, listener, errorListner);
        }

        @Override
        protected Response<List<Category>> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(JSON.parseArray(json, Category.class),
                        Connectivity.newCache(response, 30000));
            } catch (UnsupportedEncodingException e) {
                String json = new String(response.data);
                return Response.success(JSON.parseArray(json, Category.class),
                        Connectivity.newCache(response, 30000));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }

    }
