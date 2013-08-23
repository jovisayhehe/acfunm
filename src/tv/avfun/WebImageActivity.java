package tv.avfun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import tv.ac.fun.R;
import tv.avfun.app.AcApp;
import tv.avfun.util.FileUtil;
import uk.co.senab.photoview.PhotoView;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;


public class WebImageActivity extends SherlockFragmentActivity {
    private static ImageLoader imageLoader = ImageLoader.getInstance();;
    private ViewPager pager;
    private static File downlaodFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(124, 0, 0, 0)));
        setContentView(R.layout.ac_image_pager);
        Bundle extras = getIntent().getExtras();
        ArrayList<String> list = extras.getStringArrayList("images");
        getSupportActionBar().setTitle(extras.get("title").toString());
        int index = extras.getInt("index",0);
        MobclickAgent.onEvent(this,"view_bigpic");
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImageAdapter(getSupportFragmentManager(),list));
        pager.setCurrentItem(index);
        downlaodFolder = AcApp.getDownloadImagePath();
        if(!downlaodFolder.exists())
            downlaodFolder.mkdirs();
    }
    
    class ImageAdapter extends FragmentPagerAdapter{
        ArrayList<String> list;
        
        ImageAdapter(FragmentManager fm, ArrayList<String> imgUrls) {
            super(fm);
            this.list = imgUrls;
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public Fragment getItem(int position) {
            WebImageFragment fragment = new WebImageFragment();
            Bundle args = new Bundle();
            args.putIntArray("index", new int[]{position+1, getCount()});
            args.putString("image", list.get(position));
            fragment.setArguments(args);
            return fragment;
        }
        
    }
    public static class WebImageFragment extends Fragment{
        private int[] mIndexInfos;
        private String mImage;
        private PhotoView mImageView;
        private Bitmap mloadedBitmap;
        private File saveFile;
        private View mLoadView;
        public WebImageFragment(){ }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mIndexInfos = getArguments().getIntArray("index");
            mImage = getArguments().getString("image");
            saveFile = new File(downlaodFolder,FileUtil.getName(mImage));
            
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            
            View content = inflater.inflate(R.layout.fragment_web_image, container,false);
            mImageView = (PhotoView) content.findViewById(R.id.image);
            mLoadView = content.findViewById(R.id.loading);
//            TODO:
//            if(saveFile.exists())
//                content.findViewById(R.id.download).setEnabled(false);
//            else
            content.findViewById(R.id.download).setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if(mloadedBitmap !=null && !mloadedBitmap.isRecycled()){
                        
                        OutputStream out;
                        try {
                            saveFile.delete();
                            if(saveFile.createNewFile()){
                                out = new FileOutputStream(saveFile);
                                if(mloadedBitmap.compress(CompressFormat.JPEG, 90, out)){
                                    v.setEnabled(false);
                                    Toast.makeText(getActivity(), "保存至"+saveFile.getAbsolutePath(), 0).show();
                                    MobclickAgent.onEvent(getActivity(), "save_image");
                                    return;
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(getActivity(), "保存失败！", 0).show();
                }
            });
            TextView indexView = (TextView) content.findViewById(R.id.index);
            indexView.setText(String.format("%d/%d", mIndexInfos[0],mIndexInfos[1]));
            return content;
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            imageLoader.displayImage(mImage, mImageView, new ImageLoadingListener() {
                
                @Override
                public void onLoadingStarted() {
                    mLoadView.setVisibility(View.VISIBLE);
                }
                
                @Override
                public void onLoadingFailed(FailReason failReason) {
                    mImageView.setImageResource(R.drawable.no_picture);
                    switch (failReason) {
                    case IO_ERROR:
                        Toast.makeText(getActivity(), "读取失败...", 0).show();
                        break;
                    case OUT_OF_MEMORY:
                        Toast.makeText(getActivity(), "内存不足...", 0).show();
                        break;
                    default:
                        Toast.makeText(getActivity(), "未知错误...", 0).show();
                        break;
                    }
                    
                }
                
                @Override
                public void onLoadingComplete(Bitmap loadedImage) {
                    mloadedBitmap = loadedImage;
                    mLoadView.setVisibility(View.GONE);
                }
                
                @Override
                public void onLoadingCancelled() {
                }
            });
        }
        
    }
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
