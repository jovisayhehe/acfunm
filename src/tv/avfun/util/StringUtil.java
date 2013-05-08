
package tv.avfun.util;


public class StringUtil {

    public static String getSource(String escapedHtml) {
        /**
         * 字符 转义字符 “ &quot; & &amp; < &lt; > &gt; &nbsp;
         */
        return escapedHtml.replaceAll("&quot;", "\"").replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">").replaceAll("&nbsp;", " ");
    }
    
    public static boolean validate(String str){
        return str!=null && str.isEmpty() && !"null".equalsIgnoreCase(str);
    }
}
