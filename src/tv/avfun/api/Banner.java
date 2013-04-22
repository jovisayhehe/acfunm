package tv.avfun.api;

import java.io.Serializable;

import tv.avfun.entity.Contents;

public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;
    // 标识是否为文章
    public int                mediaType;
    public Contents           bannerInfo;
}
