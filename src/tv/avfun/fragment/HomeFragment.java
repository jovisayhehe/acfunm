
package tv.avfun.fragment;


import java.util.ArrayList;
import java.util.Random;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class HomeFragment extends SherlockFragment {

    private int                 randomnum;
    private int                 oldrandomnum;
    private Drawable            firstdw;
    private Drawable            secondw;
    private Activity            activity;
    private GridView            gdview;
    private MenuGridViewAdaper  gradaper;
    private ArrayList<GridItem> data;
    private View                v;
    private final Handler       handler = new Handler();
    private Random random = new Random();
    private final Runnable      task
        = new Runnable() {

            @Override
            public void run() {

                handler.postDelayed(this, 3500);
                // 简化逻辑
                while (oldrandomnum == randomnum) {
                    randomnum = random.nextInt(8);
                }
                oldrandomnum = randomnum;
                
                View view = gdview.getChildAt(randomnum);
                if (view != null) {
                    ImageView imgf = (ImageView) view.findViewById(R.id.gdviewitemimgf);
                    ImageView imgs = (ImageView) view.findViewById(R.id.gdviewitemimgs);
                    firstdw = imgf.getBackground();
                    secondw = imgs.getBackground();
                    applyRotation(-3, 180, imgf, imgs);
                }

            }
        };
    @SuppressWarnings("deprecation")
    private void applyRotation(float start, float end, ImageView imgf, ImageView imgs) {
        
        // 大小是一样的，无需各自获取，甚至可以做成成员变量
        final float centerX = imgf.getWidth() / 2.0f;

        final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, centerX, 0, 0.0f, true);
        rotation.setDuration(1000);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        if (!(Boolean) imgf.getTag()) {
            imgf.setBackgroundDrawable(firstdw);
            imgs.setBackgroundDrawable(secondw);
            // 必须clean原来的动画，否则就会出现白板的情况-_-#
            imgs.clearAnimation();
            
            imgf.startAnimation(rotation);
            imgf.setTag(true);
            imgs.setTag(false);
        } else if (!(Boolean) imgs.getTag()) {
            imgs.setBackgroundDrawable(firstdw);
            imgf.setBackgroundDrawable(secondw);
            imgf.clearAnimation();

            imgs.startAnimation(rotation);
            // 原来的逻辑错了~~~
            imgs.setTag(true);
            imgf.setTag(false);
        }
    }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home_layout, container, false);

        return v;
    }
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        this.activity = getActivity();
        data = new ArrayList<GridItem>();
        initData();

        gdview = (GridView) v.findViewById(R.id.gdview);
        gradaper = new MenuGridViewAdaper(activity);
        gdview.setAdapter(gradaper);
        gdview.setOnItemClickListener(new MenuGridViewItemLisener());
        handler.postDelayed(task, 3000);
    }
   
    private void initData() {
        GridItem anItem = new GridItem("动画", R.drawable.anf, R.drawable.ans);
        GridItem funItem = new GridItem("娱乐", R.drawable.funf, R.drawable.funs);
        GridItem gameItem = new GridItem("游戏", R.drawable.gamef, R.drawable.games);
        GridItem musicItem = new GridItem("音乐", R.drawable.musicf, R.drawable.musics);
        GridItem movieItem = new GridItem("短影", R.drawable.duanyinf, R.drawable.duanyins);
        GridItem bangumiItem = new GridItem("番剧", R.drawable.fanjuf, R.drawable.fanjus);
        GridItem articleItem = new GridItem("文章", R.drawable.ganqf, R.drawable.ganqs);

        data.add(anItem);
        data.add(musicItem);
        data.add(funItem);
        data.add(movieItem);
        data.add(gameItem);
        data.add(bangumiItem);
        data.add(articleItem);
    }

    

    private final class MenuGridViewItemLisener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(activity, Channel_Activity.class);
            intent.putExtra("position", position);
            if (position == 6) {
                intent.putExtra("isarticle", true);
            } else {

                intent.putExtra("isarticle", false);
            }

            activity.startActivity(intent);
        }

    }

    private final class MenuGridViewAdaper extends BaseAdapter {

        private LayoutInflater mInflater;

        public MenuGridViewAdaper(Context context) {
            this.mInflater = LayoutInflater.from(context);
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
            GridItem item = data.get(position);
            convertView = mInflater.inflate(R.layout.gdviewitem, null);
            ImageView img2 = (ImageView) convertView.findViewById(R.id.gdviewitemimgs);
            img2.setBackgroundResource(item.img2);
            img2.setTag(true);

            ImageView img1 = (ImageView) convertView.findViewById(R.id.gdviewitemimgf);
            img1.setBackgroundResource(item.img1);
            img1.setTag(false);

            TextView text = (TextView) convertView.findViewById(R.id.gdviewitemtext);
            text.setText(item.title);

            return convertView;
        }
    }
    // 首页条目
    static class GridItem {
        int    img1;
        int    img2;
        String title;
        
        public GridItem(String title, int img1, int img2) {
            this.img1 = img1;
            this.img2 = img2;
            this.title = title;
        }

    }

}
