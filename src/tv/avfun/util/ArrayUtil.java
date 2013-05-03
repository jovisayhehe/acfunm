package tv.avfun.util;

import java.util.List;


public class ArrayUtil {
    public static long[] toLongArray(List<Long> list){
        if(list==null || list.isEmpty()) return null;
        long[] arr = new long[list.size()];
        for(int i=0;i<list.size();i++){
            arr[i] = list.get(i).longValue();
        }
        return arr;
    }
}
