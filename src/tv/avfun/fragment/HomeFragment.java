package tv.avfun.fragment;




import java.util.ArrayList;
import java.util.HashMap;

import tv.avfun.Channel_Activity;
import tv.avfun.R;
import tv.avfun.animation.Rotate3dAnimation;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;

public class HomeFragment extends SherlockFragment{
	private GridView gdview;
	private ArrayList<HashMap<String, Object>> data;
	private MenuGridViewAdaper gradaper;
	private int randomnum;
	private int oldrandomnum;
	private Drawable firstdw;
	private Drawable secondw;
	private Activity activity;
	private View v;
	private final Handler handler = new Handler();  
    private final Runnable task = new Runnable() {  
  	  
        @Override  
        public void run() {  
              
                handler.postDelayed(this, 3500);
                
                while(oldrandomnum ==randomnum){
                	randomnum = (int)(Math.random()*8);
                	if(oldrandomnum!=randomnum){
                		break;
                	}
                }
                
                oldrandomnum = randomnum;
                View view = gdview.getChildAt(randomnum);
                if(view!=null){
                    ImageView imgf = (ImageView) view.findViewById(R.id.gdviewitemimgf);
                    ImageView imgs = (ImageView) view.findViewById(R.id.gdviewitemimgs);
                    firstdw = imgf.getBackground();
                    secondw = imgs.getBackground();
                    applyRotation(-3, 180, imgf,imgs);
                }

        }  
    };
	public static HomeFragment newInstance() {
    	HomeFragment f = new HomeFragment();
		Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
	public void onPause() {
		
		super.onPause();
		
	}
    
    

	@Override
	public void onResume() {
		
		super.onResume();
		
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.home_layout, container, false);
		

      return v;
    }

	
	
   @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		this.activity = getActivity();
		data = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        
        map1.put("img1", R.drawable.anf);
        map1.put("img2", R.drawable.ans);
        map1.put("title", "动画");
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        
        map2.put("img1", R.drawable.muiscf);
        map2.put("img2", R.drawable.muiscs);
        map2.put("title", "音乐");
        
        
        HashMap<String, Object> map3 = new HashMap<String, Object>();
        
        map3.put("img1", R.drawable.funf);
        map3.put("img2", R.drawable.funs);
        map3.put("title", "娱乐");
        
        HashMap<String, Object> map4 = new HashMap<String, Object>();
        
        map4.put("img1", R.drawable.duanyinf);
        map4.put("img2", R.drawable.duanyins);
        map4.put("title", "短影");
        
        HashMap<String, Object> map5 = new HashMap<String, Object>();
        
        map5.put("img1", R.drawable.gamef);
        map5.put("img2", R.drawable.games);
        map5.put("title", "游戏");
        
        HashMap<String, Object> map6 = new HashMap<String, Object>();
        
        map6.put("img1", R.drawable.fanjuf);
        map6.put("img2", R.drawable.fanjus);
        map6.put("title", "番剧");
        HashMap<String, Object> map8 = new HashMap<String, Object>();
        
        map8.put("img1", R.drawable.ganqf);
        map8.put("img2", R.drawable.ganqs);
        map8.put("title", "文章");
        
        
        data.add(map1);
        data.add(map2);
        data.add(map3);
        data.add(map4);
        data.add(map5);
        data.add(map6);
        data.add(map8);
        
        gdview = (GridView) v.findViewById(R.id.gdview);
        gradaper = new MenuGridViewAdaper(activity, data);
        gdview.setAdapter(gradaper);
        gdview.setOnItemClickListener(new MenuGridViewItemLisener());
        handler.postDelayed(task, 3000);
	}

@SuppressWarnings("deprecation")
   private void applyRotation(float start, float end,ImageView imgf,ImageView imgs) {
    	
    	
    	if(!(Boolean) imgf.getTag()){
            // Find the center of the container
    		
    		imgs.setBackgroundDrawable(secondw);
    		imgf.setBackgroundDrawable(firstdw);
            final float centerX = imgf.getWidth() / 2.0f;
//            final float centerY = imgf.getHeight() / 2.0f;

            final Rotate3dAnimation rotation =
                    new Rotate3dAnimation(start, end, centerX, 0, 0.0f, true);
            rotation.setDuration(1000);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new AccelerateInterpolator());
            imgf.startAnimation(rotation);
            imgf.setTag(true);
            imgs.setTag(false);
    	}else if(!(Boolean) imgs.getTag()){
    		imgs.setBackgroundDrawable(firstdw);
    		imgf.setBackgroundDrawable(secondw);
            final float centerX = imgf.getWidth() / 2.0f;
//            final float centerY = imgf.getHeight() / 2.0f;

            final Rotate3dAnimation rotation =
                    new Rotate3dAnimation(start, end, centerX, 0, 0.0f, true);
            rotation.setDuration(1000);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new AccelerateInterpolator());
            imgf.startAnimation(rotation);
            imgf.setTag(true);
            imgf.setTag(false);
    	}
    }
   
   	private final class MenuGridViewItemLisener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			
			Intent intent = new Intent(activity, Channel_Activity.class);
			intent.putExtra("position", position);
			if(position==6){
				intent.putExtra("isarticle", true);
			}else{
		
				intent.putExtra("isarticle", false);
			}

			activity.startActivity(intent);
		}
   		
   	}
   
	private final class MenuGridViewAdaper extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<HashMap<String, Object>> data;
		
		public MenuGridViewAdaper(Context context,
				ArrayList<HashMap<String, Object>> data) {
			this.mInflater = LayoutInflater.from(context);
			this.data = data;
		}
		
		@Override
		public int getCount() {
			
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			

				convertView = mInflater.inflate(R.layout.gdviewitem, null);
				ImageView img2 = (ImageView) convertView.findViewById(R.id.gdviewitemimgs);
				img2.setBackgroundResource((Integer) this.data.get(position).get("img2"));
                img2.setTag(true);
                
				ImageView img = (ImageView) convertView.findViewById(R.id.gdviewitemimgf);
				img.setBackgroundResource((Integer) this.data.get(position).get("img1"));
				img.setTag(false);
				
				TextView text =(TextView) convertView.findViewById(R.id.gdviewitemtext);
				text.setText((CharSequence) this.data.get(position).get("title"));
				
				return convertView;
		}
	}
    
}
