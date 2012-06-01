package tv.avfun;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import tv.acfun.util.AsyncImageLoader;
import tv.acfun.util.AsyncImageLoader.ImageCallback;
import tv.acfun.util.Util;
import tv.avfun.R;

import acfun.domain.Article;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Channell_ContentListViewAdaper extends BaseAdapter{
	private LayoutInflater mInflater;
	private List<Map<String, Object>> data;
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	public Channell_ContentListViewAdaper(Context context,List<Map<String, Object>> data, ListView listView) {
		this.mInflater =LayoutInflater.from(context);
		this.data = data;
		this.listView = listView;
		asyncImageLoader = new AsyncImageLoader();
	}
	
	public void setData(List<Map<String, Object>> data){
		this.data = data;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return data.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ListViewHolder holder;
		if(convertView==null){
			convertView = mInflater.inflate(R.layout.channellist_content_item, null);
			holder = new ListViewHolder();
			holder.img = (ImageView) convertView.findViewById(R.id.channellist_item_img);
			holder.title = (TextView) convertView.findViewById(R.id.channellist_content_item_title);
			holder.date = (TextView) convertView.findViewById(R.id.channelist_content_item_views);
			holder.upman = (TextView) convertView.findViewById(R.id.channelist_content_item_art);
			convertView.setTag(holder);
		}else {
			holder = (ListViewHolder) convertView.getTag();
		}
		final Map art = data.get(position); 
		holder.title.setText(String.valueOf(art.get("title")));
		holder.date.setText("点击:"+ String.valueOf(art.get("views")));
		holder.upman.setText(String.valueOf(art.get("art")));
		
		
		final String imageUrl = (String) art.get("titleimg");
		if(imageUrl!= "null"){
			holder.img.setTag(imageUrl);
	        Drawable cachedImage = asyncImageLoader.loadDrawable(imageUrl, new ImageCallback() {
	            public void imageLoaded(Drawable imageDrawable, String imageUrl) {
	                ImageView imageViewByTag = (ImageView) listView.findViewWithTag(imageUrl);
	                if (imageViewByTag != null) {
	                    imageViewByTag.setImageDrawable(imageDrawable);
	                }
	            }
	        });
			
			if (cachedImage == null) {
				holder.img.setImageResource(R.drawable.no_picture);
			}else{
				holder.img.setImageDrawable(cachedImage);
			}
		}else{
			holder.img.setImageResource(R.drawable.no_picture);
		}
		convertView.setTag(convertView.getId(),art.get("link").toString().substring(5));
		return convertView;
		
		
//		if(imageUrl!=null){
//			new Thread(){
//				public void run(){		
//					try {
//						final Drawable d;
//						
//						File f = new File(Environment.getExternalStorageDirectory()+"/acfunimg/"+String.valueOf(art.get("id")).substring(3));
//							if(imageCache.containsKey(imageUrl)){
//								SoftReference<Drawable> softReference = imageCache.get(imageUrl);
//					            
//					             if(softReference.get()!=null){
//					            	 d = softReference.get();
//					             }else{
//					            	 FileInputStream fis = new FileInputStream(f);
//									d = Drawable.createFromStream(fis, "src");
//					             }
//							}else{
//								if(!f.exists()){
//									Log.i("url", imageUrl);
//									URL m = new URL(String.valueOf(imageUrl));
//									InputStream i = (InputStream) m.getContent();
//									DataInputStream in = new DataInputStream(i);
//									FileOutputStream out = new FileOutputStream(f);
//									byte[] buffer = new byte[1024];
//									int   byteread=0;
//									while ((byteread = in.read(buffer)) != -1) {
//										out.write(buffer, 0, byteread);
//									}
//									in.close();
//									out.close();	
//									d = Drawable.createFromStream(i, "src");
//								}else{
//									FileInputStream fis = new FileInputStream(f);
//									d = Drawable.createFromStream(fis, "src");
//								}
//
//							}
//							
//						imageCache.put(imageUrl, new SoftReference<Drawable>(d));
//						acti.runOnUiThread(new Runnable() {
//							public void run() {
//								holder.img.setImageDrawable(d);
//							} 
//						});	
//						
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						acti.runOnUiThread(new Runnable() {
//							public void run() {
//								holder.img.setImageResource(R.drawable.no_picture);
//							} 
//						});	
//						e.printStackTrace();
//					}
//				}	
//			}.start();
//		}else{
//			holder.img.setImageResource(R.drawable.no_picture);
//		}
		
	}
	
	static class ListViewHolder {
		ImageView img;
		TextView title;
		TextView date;
		TextView upman;
		}
	public static Drawable loadImageFromUrl(String url) {
		URL m;
		InputStream i = null;
		try {
			m = new URL(url);
			i = (InputStream) m.getContent();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Drawable d = Drawable.createFromStream(i, "src");
		return d;
	}

}
