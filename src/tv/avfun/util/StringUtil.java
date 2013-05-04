
package tv.avfun.util;


public class StringUtil {

    public static String getSource(String escapedHtml) {
        /**
         * 字符 转义字符 “ &quot; & &amp; < &lt; > &gt; &nbsp;
         */
        return escapedHtml.replaceAll("&quot;", "\"").replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">").replaceAll("&nbsp;", " ");
    }
}
