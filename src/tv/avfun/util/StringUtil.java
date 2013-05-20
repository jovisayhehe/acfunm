
package tv.avfun.util;


public class StringUtil {

    /**
     * 字符 转义字符 “ &quot; & &amp; < &lt; > &gt; &nbsp;
     */
    public static String getSource(String escapedHtml) {
        return escapedHtml.replaceAll("&quot;", "\"").replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">").replaceAll("&nbsp;", " ");
    }
    
    public static boolean validate(String str){
        return str!=null && str.length() >0 && !"null".equalsIgnoreCase(str);
    }
}
