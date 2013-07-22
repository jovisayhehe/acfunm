
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class CommentsAdaper2 extends BaseAdapter {

    private LayoutInflater       mInflater;
    private TreeMap<Integer ,Comment> data;
    private List<Comment> comments = new ArrayList<Comment>();
    private Context mContext;

    public CommentsAdaper2(Context context, TreeMap<Integer ,Comment>data) {
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

    private int frameId = R.id.comment_frame;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView != null && convertView instanceof ViewGroup){
            ((ViewGroup)convertView).removeAllViews();
            convertView = null;
        }
        // TODO 多个评论共同引用同一条的情况
        Comment c = getItem(position);
        //Log.i("comments", "--------position="+position);
        RelativeLayout frame = (RelativeLayout) mInflater.inflate(R.layout.comments_listitem, null);
        TextView username = (TextView) frame.findViewById(R.id.user_name);
        username.setText("#"+c.count+" "+c.userName);
        TextView content = (TextView) frame.findViewById(R.id.comments_content);
        setContent(content,c);
        int quoteId = c.quoteId;
        Comment quote = data.get(quoteId);
        int anchor;
        if (quote != null) {
            if(quote.isQuoted){
                if(quote.beQuotedPosition == position){
                    handleQuote(quote,frame,10,position);
                    anchor = frameId;
                }else{
                    frame.findViewById(R.id.requote).setVisibility(View.VISIBLE);
                    anchor = R.id.requote;
                }
            }else{
                quote.isQuoted = true;
                quote.beQuotedPosition = position;
                handleQuote(quote,frame,10,position);
                anchor = frameId;
            }
            if(anchor!= R.id.requote){
                RelativeLayout.LayoutParams userLayoutParams = (LayoutParams) username.getLayoutParams();
                userLayoutParams.addRule(RelativeLayout.BELOW, anchor);
                username.setLayoutParams(userLayoutParams);
            }
        }
        return frame;
    }

    private void setContent(TextView comment, Comment c) {
        String text = c.content;
        text = replace(text);
        
        comment.setText(Html.fromHtml(text,new ImageGetter() {
            
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
        comment.setTextColor(Color.BLACK);
        Pattern http = Pattern.compile("(http://(?:[a-z0-9.-]+[.][a-z]{2,}+(?::[0-9]+)?)(?:/[^\\s\u3010\u4e00-\u9fa5]*)?)",
                Pattern.CASE_INSENSITIVE);
        Linkify.addLinks(comment, http, "http://");
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
    private void handleQuote(Comment quote, RelativeLayout commentFrame, int deep,int position){
        
        RelativeLayout quoteFrame = (RelativeLayout) mInflater.inflate(R.layout.comments_listitem, null);
        quoteFrame.setBackgroundResource(R.drawable.comment_bg);
        RelativeLayout.LayoutParams params2 = new LayoutParams(-1, -2);
//        params2.addRule(RelativeLayout.ABOVE, R.id.user_name);
        TextView username  = (TextView) quoteFrame.findViewById(R.id.user_name);
        username.setText("#"+quote.count+" "+quote.userName);
        commentFrame.addView(quoteFrame, params2);
        TextView content = (TextView) quoteFrame.findViewById(R.id.comments_content);
        setContent(content,quote);
        int quoteId = quote.quoteId;
        deep -= 1;
        Comment quote2 = data.get(quoteId);
        if (quote2 != null && deep >0) {
            if(quote2.isQuoted){
                if(quote2.beQuotedPosition == position){
                    handleQuote(quote2,quoteFrame,deep,position);
                }
            }else{
                quote2.isQuoted = true;
                quote2.beQuotedPosition = position;
                handleQuote(quote2,quoteFrame,deep,position);
            }
            RelativeLayout.LayoutParams userLayoutParams = (LayoutParams) username.getLayoutParams();
            userLayoutParams.addRule(RelativeLayout.BELOW, frameId);
            username.setLayoutParams(userLayoutParams);
        }
    }

}
