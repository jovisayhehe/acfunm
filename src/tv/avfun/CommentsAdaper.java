
package tv.avfun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ac.fun.R;
import tv.avfun.entity.Comment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.util.Linkify;
import android.text.SpannedString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class CommentsAdaper extends BaseAdapter {

    private LayoutInflater       mInflater;
    private TreeMap<Integer ,Comment> data;
    private List<Comment> comments = new ArrayList<Comment>();
    private Context mContext;

    public CommentsAdaper(Context context, TreeMap<Integer ,Comment>data) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.data = data;
        for(Map.Entry<Integer ,Comment> e : data.entrySet()){
            comments.add(e.getValue());
        }
    }

    public void setData(TreeMap<Integer ,Comment> data) {
        this.data = data;
        comments.clear();
        for(Map.Entry<Integer ,Comment> e : data.entrySet()){
            comments.add(e.getValue());
        }
    }

    @Override
    public int getCount() {

        return data.size();
    }

    @Override
    public Comment getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    private int frameId = R.id.comments_content;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment c = getItem(position);
        RelativeLayout frame = (RelativeLayout) mInflater.inflate(R.layout.comment_frame, null);
        TextView username = (TextView) frame.findViewById(R.id.user_name);
        TextView floor = (TextView) frame.findViewById(R.id.floor);
        username.setText(c.userName);
        floor.setText("#" + c.count);
        View comment = genContent(c);
        
        int quoteId = c.quoteId;
        int anchor;
        
        Comment quote = data.get(quoteId);
        
        if (quote != null) {
            handleQuote(quote,frame);
            anchor = frameId;
        } else
            anchor = R.id.floor;
        
        RelativeLayout.LayoutParams params = new LayoutParams(-2, -2);
        params.addRule(RelativeLayout.BELOW, anchor);
        frame.addView(comment, params);
        
        
        return frame;
    }

    private View genContent(Comment c) {
        TextView content = new TextView(mContext);
        String text = c.content;
        text = replace(text);
        
        content.setText(Html.fromHtml(text,new ImageGetter() {
            
            @Override
            public Drawable getDrawable(String source) {
                try {
                    Drawable drawable = Drawable.createFromStream(mContext.getAssets().open(source),source);
                    if(drawable!=null)
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    return drawable;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                
            }
        },null));
        content.setTextColor(Color.BLACK);
        Pattern http = Pattern.compile("(http://(?:[a-z0-9.-]+[.][a-z]{2,}+(?::[0-9]+)?)(?:/[^\\s\u3010\u4e00-\u9fa5]*)?)",
                Pattern.CASE_INSENSITIVE);
        Linkify.addLinks(content, http, "http://");
        return content;
    }

    private String replace(String text) {
        String reg = "\\[emot=(.*?),(.*?)\\/\\]";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(text);
        while(m.find()){
            String id =  m.group(2);
            // FIXME: id 50 以上的表情
            if(Integer.parseInt(id)>50)
                id = "50";
            text = text.replace(m.group(),"<img src='emotion/"+id+".png' />");
        }
        reg = "\\[at\\](.*?)\\[\\/at\\]";
        m = Pattern.compile(reg).matcher(text);
        while(m.find()){
            text = text.replace(m.group(), "@" + m.group(1));
        }
        return text;
    }
    private void handleQuote(Comment quote, RelativeLayout frame){
        RelativeLayout frame2 = (RelativeLayout) mInflater.inflate(R.layout.comment_frame, null);
        frame2.setBackgroundResource(R.drawable.comment_bg);
        RelativeLayout.LayoutParams params2 = new LayoutParams(-1, -2);
        params2.addRule(RelativeLayout.BELOW, R.id.floor);
        frame.addView(frame2, params2);

        ((TextView) frame2.findViewById(R.id.user_name)).setText(quote.userName);
        ((TextView) frame2.findViewById(R.id.floor)).setText("#" + quote.count);
        View content = genContent(quote);
        int quoteId = quote.quoteId;
        int anchor;
        
        Comment quote2 = data.get(quoteId);
        if (quote2 != null) {
            handleQuote(quote2,frame2);
            anchor = frameId;
        } else
            anchor = R.id.floor;
        
        RelativeLayout.LayoutParams params = new LayoutParams(-2, -2);
        params.addRule(RelativeLayout.BELOW, anchor);
        frame2.addView(content, params);
        frame2.setId(frameId);
    }

}
