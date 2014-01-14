package tv.acfun.video.fragment;

import java.util.List;

import tv.acfun.video.AcApp;
import tv.acfun.video.DetailsActivity;
import tv.ac.fun.R;
import tv.acfun.video.adapter.BaseArrayAdapter;
import tv.acfun.video.api.API;
import tv.acfun.video.db.DB;
import tv.acfun.video.entity.Category;
import tv.acfun.video.entity.Video;
import tv.acfun.video.entity.Videos;
import tv.acfun.video.util.DensityUtil;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.net.FastJsonRequest;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
    private int mCurrentPage;
    public VideosFragment() {
    }
    public static Fragment newInstance(Category cat) {
        return newInstance(cat.id);
    }
    
    public static Fragment newInstance(int catId){
        VideosFragment f = new VideosFragment();
        Bundle args = new Bundle();
        if(catId > 1024)
            catId = 1024;
        args.putInt(API.EXTRAS_CATEGORY_ID,catId);
        f.setArguments(args);
        f.setRetainInstance(true);
        return f;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDb = new DB(mActivity);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        catId = getArguments().getInt(API.EXTRAS_CATEGORY_ID);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mGridView.setColumnWidth(400);
        
        setNumColumns();
        
    }
    
    private void setNumColumns() {
        int w = getResources().getDisplayMetrics().widthPixels;
        int n = w / getResources().getDimensionPixelSize(R.dimen.item_width);
        if (n < 2) n = 2;
        mGridView.setNumColumns(n);
    }

    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setNumColumns();
    }


    Listener<Videos> listener = new Listener<Videos>() {

        @Override
        public void onResponse(Videos response) {
            if(response.pageNo ==1){
                ListAdapter adapter = new VideosAdapter(mActivity, response.list);
                setAdapter(adapter);
            }else{
                Toast.makeText(mActivity, String.format("成功加载%d 条数据",response.pageSize), 0).show();
                ((VideosAdapter)mAdapter).addData(response.list);
            }
        }
        
    };
    ErrorListener errListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO : 提示错误
            
            
        }};
    private DB mDb;
    
    private Request<?> newRequest(int page){
        Request<?> request =  new VideosRequest(catId,page,true,listener,errListener);
        request.setShouldCache(true);
        return request;
    }
    @Override
    protected Request<?> newRequest() {
        mCurrentPage = 0;
        return newRequest(mCurrentPage);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Video item = (Video) parent.getItemAtPosition(position);
            mDb.insertHistory(item);
            Object tag = view.getTag();
            if(tag != null && tag instanceof ViewHolder){
                ViewHolder holder =( ViewHolder)tag;
                holder.titleView.setTextColor(0xFF666666);
            }
            DetailsActivity.start(mActivity, item);
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
//                holder.descView = (TextView) convertView.findViewById(android.R.id.text2);
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
            holder.titleView.setTextColor(mDb.isWatched(item.acId)?0xFF666666:0xFF000000);
            
//            String desc = item.creator.name /*+ "/"+ item.viewernum + "次播放"*/;
//            holder.descView.setText(desc);
        }
        
    }
    @Override
    protected void onLastItemVisible() {
        Request<?> request = newRequest(++mCurrentPage);
        AcApp.addRequest(request);
    }
   
    private static class ViewHolder {
        TextView titleView,descView;
        ImageView imageView;
        ImageLoader.ImageContainer imageContainer;
    }
}