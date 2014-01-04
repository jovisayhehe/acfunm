package tv.acfun.video.player.resolver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.acfun.video.BuildConfig;
import tv.acfun.video.player.MediaList;
import tv.acfun.video.player.MediaSegment;
import android.content.Context;
import android.util.Log;

public class YoukuResolver extends BaseResolver{

    private static final String TAG = "YoukuResolver";
   
    
    // TODO: change parsing mode
    /**
     * <pre>
     * 
     * hd3|hd2|mp4|flv
     * ---------------
     *  3 | 2 | 1 | 0
     *  </pre>
     */
    private int mode = 1;

    public YoukuResolver(String vid) {
        super(vid);
    }

    @Override
    public void resolve(Context context) {
        String url = "http://v.youku.com/player/getPlayList/VideoIDS/" + vid;
        if (BuildConfig.DEBUG)
            Log.i(TAG, "尝试获取youku :" + vid);
        try {
            JSONObject jsonObject = getJSONObject(url);
            if (jsonObject == null)
                throw new JSONException("null");
            JSONObject data = jsonObject.getJSONArray("data").getJSONObject(0);
            Double seed = data.getDouble("seed");
            JSONObject fileids = data.getJSONObject("streamfileids");

            String seg = null;
            String fids = null;
            // TODO : the hd3 mode
            if (mode >= 2 && fileids.has("hd2")) {
                seg = "hd2";
            } else if (mode >= 1 && fileids.has("mp4")) {
                seg = "mp4";
            } else if (fileids.has("flv")) {
                seg = "flv";
            }
            fids = fileids.getString(seg);
            String realFileid = getFileID(fids, seed);

            JSONObject segs = data.getJSONObject("segs");

            JSONArray vArray = segs.getJSONArray(seg);

            String vPath = seg.equals("mp4") ? "mp4" : "flv";
            for (int i = 0; i < vArray.length(); i++) {
                JSONObject part = vArray.getJSONObject(i);
                String k = part.getString("k");
                String k2 = part.getString("k2");
                MediaSegment s = new MediaSegment();
                long duration = (long) (Float.parseFloat(part.getString("seconds")) * 1000);
                long size = part.getLong("size");
                String u = "http://f.youku.com/player/getFlvPath/sid/" + System.currentTimeMillis()
                        + "_" + String.format("%02d", i) + "/st/" + vPath + "/fileid/"
                        + realFileid.substring(0, 8) + String.format("%02X", i)
                        + realFileid.substring(10) + "?K=" + k + ",k2:" + k2;
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "url= " + u);
                s.mDuration = duration;
                s.mSize = size;
                s.mUrl = u;
                mList.add(s);
            }
            handler.sendEmptyMessage(ARG_OK);
            
        } catch (JSONException e) {
            if (BuildConfig.DEBUG)
                Log.w(TAG, "解析视频地址失败" + url, e);
            handler.sendEmptyMessage(ARG_ERROR);
        }
    }

    @Override
    public void resolveAsync(Context context) {
        new Thread() {
            public void run() {
                resolve(null);
            }
        }.start();
    }


    public static String getFileID(String fileid, double seed) {
        String mixed = getFileIDMixString(seed);
        String[] ids = fileid.split("\\*");
        StringBuilder realId = new StringBuilder();
        int idx;
        for (int i = 0; i < ids.length; i++) {
            idx = Integer.parseInt(ids[i]);
            realId.append(mixed.charAt(idx));
        }
        return realId.toString();
    }

    public static String genSid() {
        int i1 = (int) (1000 + Math.floor(Math.random() * 999));
        int i2 = (int) (1000 + Math.floor(Math.random() * 9000));
        return System.currentTimeMillis() + "" + i1 + "" + i2;
    }

    public static String genKey(String key1, String key2) {
        int key = Long.valueOf("key1", 16).intValue();
        key ^= 0xA55AA5A5;
        return "key2" + Long.toHexString(key);
    }

    public static String getFileIDMixString(double seed) {
        StringBuilder mixed = new StringBuilder();
        StringBuilder source = new StringBuilder(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/\\:._-1234567890");
        int index, len = source.length();
        for (int i = 0; i < len; ++i) {
            seed = (seed * 211 + 30031) % 65536;
            index = (int) Math.floor(seed / 65536 * source.length());
            mixed.append(source.charAt(index));
            source.deleteCharAt(index);
        }
        return mixed.toString();
    }

    @Override
    public MediaList getMediaList(int resolution) {
        // TODO Auto-generated method stub
        
        return null;
    }
}
