package tv.avfun.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Calendar;
import java.util.List;

import tv.avfun.AcApp;
import tv.avfun.BuildConfig;
import tv.avfun.api.Bangumi;
import tv.avfun.api.BangumiList;
import tv.avfun.api.Channel;
import tv.avfun.api.ChannelList;
import android.text.TextUtils;
import android.util.Log;

/**
 * 数据获取与缓存。单例
 * 
 * @author Yrom
 * 
 */
public class DataStore {

    private File                   timeListCachedFile, channelListCachedFile;
    private static final String    TAG                  = DataStore.class.getSimpleName();
    private static final DataStore instance             = new DataStore();

    private ChannelList            channelList          = new ChannelList();
    private BangumiList            bangumiList          = new BangumiList();
    /** 24 hours */
    public static final long       CHANNEL_LIST_EXPIRED = 24 * 60 * 60 * 1000;
    /** 3 days (XXX: 待定) */
    public static final long       TIME_LIST_EXPIRED    = 3 * CHANNEL_LIST_EXPIRED;
    /** 首页频道列表缓存文件 */
    public static final String     CHANNEL_LIST_CACHE   = "channel_list.dat";
    /** 番组列表缓存文件 */
    public static final String     TIME_LIST_CACHE      = "time_date.dat";

    private DataStore() {
        initCache();
    }

    private void initCache() {
        timeListCachedFile = new File(AcApp.context().getCacheDir(), TIME_LIST_CACHE);
        channelListCachedFile = new File(AcApp.context().getCacheDir(), CHANNEL_LIST_CACHE);
    }

    public static DataStore getInstance() {
        return instance;
    }

    // =======================================================
    // 番组列表
    // =======================================================
    /**
     * @return 没有缓存文件，或当前为星期天 <br>
     *         或者缓存时间超过 {@link #TIME_LIST_EXPIRED}，返回false
     */
    public boolean isBangumiListCached() {
        synchronized (this.bangumiList) {
            if (this.bangumiList.bangumiTimeList == null) {
                if (!readTimeListCache())
                    return false; // no cache
            }
            int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            return this.bangumiList.cacheTime + TIME_LIST_EXPIRED >= System.currentTimeMillis()
                    && dayOfWeek != 1;
        }
    }

    /** 加载缓存的番组列表 */
    public List<Bangumi[]> loadTimeList() {
        synchronized (this.bangumiList) {
            if (this.bangumiList.bangumiTimeList == null) {
                readTimeListCache();
            }
            return this.bangumiList.bangumiTimeList;
        }
    }

    private boolean readTimeListCache() {
        Object obj = readObject(timeListCachedFile.getAbsolutePath());
        if (obj != null && obj instanceof BangumiList) {
            this.bangumiList.bangumiTimeList = ((BangumiList) obj).bangumiTimeList;
            this.bangumiList.cacheTime = ((BangumiList) obj).cacheTime;
            return true;
        }
        return false;
    }

    /**
     * 保存番组列表到缓存文件
     * 
     * @param list
     *            - 当天的番组列表
     * @return list为空或保存失败则返回false
     */
    public boolean saveTimeList(List<Bangumi[]> list) {
        if (list != null) {
            this.bangumiList.bangumiTimeList = list;
            this.bangumiList.cacheTime = System.currentTimeMillis();
            return writeObject(timeListCachedFile.getAbsolutePath(), this.bangumiList);
        }
        return false;
    }

    // =======================================================
    // 首页频道列表
    // =======================================================
    /**
     * @return 没有缓存文件，或缓存时间超过 {@link #CHANNEL_LIST_EXPIRED}<br>
     *         则返回false
     */
    public boolean isChannelListCached() {
        synchronized (this.channelList) {
            if (this.channelList.channels == null) {
                if (!readChannelListCache())
                    return false;
            }
            return this.channelList.cacheTime + CHANNEL_LIST_EXPIRED >= System.currentTimeMillis();
        }
    }

    /** 加载频道列表缓存 */
    public Channel[] loadChannelList() {
        synchronized (this.channelList) {
            if (this.channelList.channels == null) {
                readChannelListCache();
            }
            return this.channelList.channels;
        }
    }

    private boolean readChannelListCache() {
        Object obj = readObject(channelListCachedFile.getAbsolutePath());
        if (obj != null && obj instanceof ChannelList) {
            this.channelList.channels = ((ChannelList) obj).channels;
            this.channelList.cacheTime = ((ChannelList) obj).cacheTime;
            return true;
        }
        return false;

    }

    /**
     * 保存频道列表到缓存文件
     * 
     * @param list
     *            - 当天的频道列表
     * @return list为空或保存失败则返回false
     */
    public boolean saveChannelList(Channel[] list) {
        if (list != null) {
            this.channelList.channels = list;
            this.channelList.cacheTime = System.currentTimeMillis();
            return writeObject(channelListCachedFile.getAbsolutePath(), this.channelList);
        }
        return false;
    }

    // =======================================================
    // 静态方法
    // =======================================================

    /**
     * 读取缓存的对象
     * 
     * @param path
     *            - 缓存的对象文件路径
     * @return 返回null，如果读取失败。
     */
    public static Object readObject(String path) {
        if (TextUtils.isEmpty(path.trim()))
            throw new IllegalArgumentException("path 不能为空！");
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
            Object obj = in.readObject();
            in.close();
            return obj;
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "failed to read object from " + path, e);
            return null;
        }
    }

    /**
     * 写到缓存文件
     * 
     * @param path
     *            - 文件路径
     * @param obj
     *            - 缓存的对象
     * @return false，如果写失败
     */
    public static boolean writeObject(String path, Serializable obj) {
        if(obj == null) 
            throw new IllegalArgumentException("obj can not be null");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(obj);
            out.close();
            return true;
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "can not write obj to " + path, e);
        }
        return false;

    }

    /**
     * 读取字符串
     * 
     * @return 文件不存在或读取失败，返回null
     */
    public static String readFromFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                String str = new String(readDataFromFile(file), "utf-8");
                return str;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "can not read from file " + path, e);
        }
        return null;
    }

    /**
     * 写字符串到文件
     * 
     * @param path
     *            - 文件路径
     * @param str
     *            - 字符串，not null
     * @return 写成功返回true
     */
    public static boolean writeToFile(String path, String str) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(str.trim()))
            throw new IllegalArgumentException("path 或 str 不能为null、空白或空字符串！");
        File file = new File(path);
        try {
            Writer writer = new FileWriter(file);
            writer.write(str);
            writer.close();
            return true;
        } catch (IOException e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "can not write to file " + path, e);
        }
        return false;
    }
    
    /** 读取字节数组 */
    public static byte[] readDataFromFile(File file) throws IOException {
        return readData(new FileInputStream(file));
    }

    /** 读取字节数组 */
    public static byte[] readData(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len = 0;
        while ((len = in.read(buffer)) != -1)
            baos.write(buffer, 0, len);
        in.close();
        return baos.toByteArray();
    }
    public static String readData(InputStream in, String charset) throws IOException{
        return new String(readData(in),charset);
    }
    
}
