package tv.avfun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tv.avfun.db.DBService;
import tv.avfun.util.ExtendedImageDownloader;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.umeng.analytics.MobclickAgent;

public class ImagePagerActivity extends SherlockActivity{
	private String channelid;
	private boolean isfavorite = false;
	private String title;
	private String aid;
	private ArrayList<String> imgUrls = new ArrayList<String>();
	private boolean instanceStateSaved;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private ViewPager pager;
	private final static int COMMID = 500;
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_image_pager);
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.memoryCacheSize(2 * 1024 * 1024) // 2 Mb
		.denyCacheImageMultipleSizesInMemory()
		.discCacheFileNameGenerator(new Md5FileNameGenerator())
		.imageDownloader(new ExtendedImageDownloader(getApplicationContext()))
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.enableLogging() // Not necessary in common
		.build();
		
		ImageLoader.getInstance().init(config);
		
		imageLoader = ImageLoader.getInstance();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		title = getIntent().getStringExtra("title");
		aid = getIntent().getStringExtra("aid");
		channelid = getIntent().getStringExtra("channelId");
		imgUrls = getIntent().getStringArrayListExtra("imgs");
		getSupportActionBar().setTitle(title);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
		new DBService(this).addtoHis(aid, title, sdf.format(new Date()),1,Integer.parseInt(channelid));
		isfavorite = new DBService(this).isFoved(aid);
		
		options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.no_picture)
		.resetViewBeforeLoading()
		.cacheOnDisc()
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.displayer(new FadeInBitmapDisplayer(300))
		.build();
		
		if(imgUrls.isEmpty()||imgUrls.size()==0){
			Toast.makeText(this, "尚未发现图片", Toast.LENGTH_SHORT).show();
		}else{
			pager = (ViewPager) findViewById(R.id.pager);
			pager.setAdapter(new ImagePagerAdapter(imgUrls));
			pager.setCurrentItem(0);
		}
		
		
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);
    	MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareIntent(createShareIntent());
    	
        if(isfavorite){
        	 menu.findItem(R.id.menu_item_fov_action_provider_action_bar).setIcon(R.drawable.rating_favorite_p);
        }
        
		 menu.add(1, ImagePagerActivity.COMMID, 1,"评论")
	      .setIcon(R.drawable.social_chat)
	      .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    
    private Intent createShareIntent() {
    	String shareurl = title+"http://www.acfun.tv/v/ac"+aid;
		Intent shareIntent = new Intent(Intent.ACTION_SEND);  
		shareIntent.setType("text/plain");  
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");  
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareurl);  
        return shareIntent;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		instanceStateSaved = true;
	}
	
	@Override
	protected void onDestroy() {
		if (!instanceStateSaved) {
			imageLoader.stop();
		}
		super.onDestroy();
	}
    
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
			
		case R.id.menu_item_fov_action_provider_action_bar:
			if(isfavorite){
				new DBService(this).delFov(aid);
				isfavorite = false;
				item.setIcon(R.drawable.rating_favorite);
				Toast.makeText(this, "取消成功", Toast.LENGTH_SHORT).show();
			}else{
				new DBService(this).addtoFov(aid, title, 0, Integer.parseInt(channelid));
				isfavorite = true;
				item.setIcon(R.drawable.rating_favorite_p);
				Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
			}

			break;
		case ImagePagerActivity.COMMID:
			
			Intent intent = new Intent(ImagePagerActivity.this, CommentsActivity.class);
			intent.putExtra("aid", aid);
			startActivity(intent);
			
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private class ImagePagerAdapter extends PagerAdapter {

		private ArrayList<String> images;
		private LayoutInflater inflater;

		ImagePagerAdapter(ArrayList<String> images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			View rootView = (View) object;
			ImageView imageView = (ImageView) rootView.findViewById(R.id.image);
			BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
			if (drawable != null) {
				Bitmap bitmap = drawable.getBitmap();
				if (bitmap != null) {
					bitmap.recycle();
				}
			}

			((ViewPager) container).removeView(rootView);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object instantiateItem(View view, int position) {
			final View imageLayout = inflater.inflate(R.layout.item_pager_image, null);
			final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			imageLoader.displayImage(images.get(position), imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted() {
					spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(FailReason failReason) {
					String message = null;
					switch (failReason) {
						case IO_ERROR:
							message = "图片被删除";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";
							break;
					}
					Toast.makeText(ImagePagerActivity.this, message, Toast.LENGTH_SHORT).show();

					spinner.setVisibility(View.GONE);
					imageView.setImageResource(R.drawable.no_picture);
				}

				@Override
				public void onLoadingComplete(Bitmap loadedImage) {
					spinner.setVisibility(View.GONE);
					PhotoViewAttacher attacher = new PhotoViewAttacher(imageView);
					attacher.update();
				}
			});

			((ViewPager) view).addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}
	}
	public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
