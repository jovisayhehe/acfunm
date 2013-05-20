package tv.avfun.api;

import java.io.Serializable;

/**
 * Channel list.
 * @author Yrom
 *
 */
public class ChannelList implements Serializable {
    private static final long serialVersionUID = 1L;
    public long cacheTime;
    public Channel[] channels;
    public String displayMode;
}
