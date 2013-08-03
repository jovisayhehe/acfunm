package tv.avfun.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.avfun.entity.Comment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.util.Linkify;
import android.widget.TextView;

public class TextViewUtils {
	
	public static void setCommentContent(final TextView comment, Comment c) {
        String text = c.content;
        text = replace(text);
        comment.setText(Html.fromHtml(text,new ImageGetter() {
            
            @Override
            public Drawable getDrawable(String source) {
                try {
                    Drawable drawable = Drawable.createFromStream(comment.getContext().getAssets().open(source),source);
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
        Pattern http = Pattern.compile("(http://(?:[a-z0-9.-]+[.][a-z]{2,}+(?::[0-9]+)?)(?:/[^\\s\u3000-\u9fe0]*)?)",
                Pattern.CASE_INSENSITIVE);
        Linkify.addLinks(comment, http, "http://");
        Linkify.addLinks(comment, Pattern.compile("(ac\\d{5,})", Pattern.CASE_INSENSITIVE), "av://");
    }

    private static String replace(String text) {
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
        reg = "\\[color=(.*?)\\]";
        m = Pattern.compile(reg).matcher(text);
        while (m.find()){
            text = text.replace(m.group(), "<font color=\"" + m.group(1) + "\" >");
        }
        text = text.replace("[/color]", "</font>");
        
        text = text.replace("\\[size=.*?\\]", "<b>").replace("[/size]", "</b>");
        
        reg = "\\[img=(.*?)\\]";
        m = Pattern.compile(reg).matcher(text);
        while (m.find()){
            text = text.replace(m.group(), m.group(1));
        }
        text = text.replace("[/img]", "");
        
        text = text.replaceAll("\\[ac=\\d{5,}\\]", "").replace("[/ac]", "");
        text = text.replace("[b]", "<strong>").replace("[/b]", "</strong>");
        return text;
    }
}
