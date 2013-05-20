package tv.avfun.util;

import java.util.ArrayList;
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
    public static <E> ArrayList<E> newArrayList(){
        return new ArrayList<E>();
    }
    public static <E> boolean validate(List<E> list){
        return list != null && !list.isEmpty();
    }
}
