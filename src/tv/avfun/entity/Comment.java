package tv.avfun.entity;

import java.io.Serializable;

/**
 *  "cid": 14787054,
      "content": "专门来看大印的啊,肿么没有了,强烈要求下期大印回归[emot\u003dac,36/]",
      "userName": "搞基稳爽不亏",
      "userID": 418134,
      "postDate": "2013-07-19 21:29:22",
      "userImg": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201211/291908515y9k.jpg",
      "userClass": "",
      "quoteId": 14786557,
      "count": 223,
      "ups": 0,
      "downs": 0
 * @author Yrom
 *
 */
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    public String content;
    public String userName;
    public String postDate;
    public String userImgUrl;
    public String userClass;
    /** 目前来看，应该不会越界...(TODO: long)*/
    public int cid;
    public int quoteId;
    public int count;
    public int ups;
    public int downs;
    public long userID;
    
    public boolean isQuoted;
    public int beQuotedPosition;
}
