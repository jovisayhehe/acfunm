package tv.acfun.video.fragment;

import java.util.List;

import tv.acfun.video.AcApp;
import tv.acfun.video.HomeActivity;
import tv.acfun.video.PlayerActivity;
import tv.acfun.video.R;
import tv.acfun.video.adapter.BaseArrayAdapter;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Category;
import tv.acfun.video.entity.Video;
import tv.acfun.video.entity.Videos;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.net.FastJsonRequest;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;

/**
 * 分类视频列表
 * 
 * @author Yrom
 *
 */
public class VideosFragment extends GridFragment{
    private int catId;
    protected Context mActivity;
    public VideosFragment() {
    }
    public static Fragment newInstance(Category cat) {
        VideosFragment f = new VideosFragment();
        Bundle args = new Bundle();
        if(cat.id > 1024)
            args.putInt(API.EXRAS_CATEGORY_ID, 1024);
        else
            args.putInt(API.EXRAS_CATEGORY_ID,cat.id);
        // TODO 
        f.setArguments(args);
        f.setRetainInstance(true);
        return f;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        catId = getArguments().getInt(API.EXRAS_CATEGORY_ID);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isLandScape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//        mGridView.setColumnWidth(400);
    }
    Listener<Videos> listener = new Listener<Videos>() {

        @Override
        public void onResponse(Videos response) {
            // TODO Auto-generated method stub
            ListAdapter adapter = new VideosAdapter(mActivity, response.list);
            setAdapter(adapter);
        }
        
    };
    ErrorListener errListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
            
        }};
    
    private Request<?> newRequest(int page){
        Request<?> request =  new VideosRequest(catId,page,true,listener,errListener);
        request.setShouldCache(true);
        return request;
    }
    @Override
    protected Request<?> newRequest() {
        return newRequest(0);
    }
    @Override
    public void onHeaderClick(AdapterView<?> parent, View view, long id) {
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Video item = (Video) parent.getItemAtPosition(position);
            PlayerActivity.start(mActivity, item);
        } catch (Exception e) {
            // TODO: handle exception
        }
        
    }
    
    public static class VideosRequest extends FastJsonRequest<Videos> {

        public VideosRequest(int catId,int page, boolean isoriginal, Listener<Videos> listener, ErrorListener errorListner) {
            super(API.getVideosUrl(catId, page, isoriginal), Videos.class, listener, errorListner);
        }
    }
    
    private class VideosAdapter extends BaseArrayAdapter<Video>{
        
        public VideosAdapter(Context context, List<Video> items) {
            super(context, items);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_videos, parent, false);
                holder = new ViewHolder();
                holder.titleView = (TextView) convertView.findViewById(android.R.id.text1);
                holder.descView = (TextView) convertView.findViewById(android.R.id.text2);
                holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Video item = getItem(position);
            setItemText(holder,item);
            handleImageRequest(holder, item);
            return convertView;
        }

        private void handleImageRequest(ViewHolder holder, Video item) {
            if (holder.imageContainer != null) {
                holder.imageContainer.cancelRequest();
            }
            ImageListener imageListener =
                    ImageLoader.getImageListener(holder.imageView, R.drawable.cover_night,R.drawable.cover_night);
            holder.imageContainer =
                    AcApp.getGloableLoader().get(item.previewurl,imageListener);
        }

        private void setItemText(ViewHolder holder, Video item) {
            String name = "无题";
            if (item.name != null) {
                name = TextViewUtils.getSource(item.name);
            }
            holder.titleView.setText(name);
//            /*
//             * 屏幕方向改变时重置textView lines
//             */
//            if(isLandScape)
//                holder.titleView.setLines(2);
//            else
//                holder.titleView.setMinLines(1);
            
            String desc = item.creator.name + " · "+ item.viewernum + "次观看";
            holder.descView.setText(desc);
        }
        
    }
    private boolean isLandScape;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        isLandScape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
//        if(mAdapter != null)
//            ((BaseAdapter)mAdapter).notifyDataSetChanged();
    }
    private String getChannelName(int id){
        if(mActivity instanceof HomeActivity){
            return ((HomeActivity)mActivity).findChannelNameById(id);
        }
        return null;
    }
    private static class ViewHolder {
        TextView titleView,descView;
        ImageView imageView;
        ImageLoader.ImageContainer imageContainer;
    }
}