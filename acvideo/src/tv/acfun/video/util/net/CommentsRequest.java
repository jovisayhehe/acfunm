
package tv.acfun.video.util.net;

import java.util.Iterator;

import tv.acfun.util.net.Connectivity;
import tv.acfun.util.net.CustomUARequest;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Comment;
import tv.acfun.video.entity.Comments;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class CommentsRequest extends CustomUARequest<Comments> {
    private static final String TAG = CommentsRequest.class.getSimpleName();

    public CommentsRequest(Context context, int aid, int page, Listener<Comments> listener, ErrorListener errListener) {
        super(API.getCommentUrl(context, aid, page), Comments.class, listener, errListener);
    }

    @Override
    protected Response<Comments> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject parseObject = JSON.parseObject(json);
            Comments comments = JSON.toJavaObject(parseObject, Comments.class);
            JSONObject commentContentArr = parseObject.getJSONObject("commentContentArr");
            comments.commentArr = parseContentAttr(commentContentArr);
            return Response.success(comments, cache(response));
        } catch (Exception e) {
            Log.e(TAG, "parse article error", e);
            return Response.error(new ParseError(e));
        }
    }

    private SparseArray<Comment> parseContentAttr(JSONObject commentContentArr) {
        SparseArray<Comment> attr = new SparseArray<Comment>();
        for (Iterator<String> iterator = commentContentArr.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next();
            JSONObject content = commentContentArr.getJSONObject(key);
            Comment comment = JSON.toJavaObject(content, Comment.class);
            attr.put(comment.cid, comment);
        }
        return attr;
    }

    private Cache.Entry cache(NetworkResponse response) {
        return Connectivity.newCache(response, 60);
    }
}