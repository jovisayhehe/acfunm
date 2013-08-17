package tv.avfun.util;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;


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
    public static <E> SparseArray<E> putAll(SparseArray<E> source, SparseArray<E> dest){
        if(dest == null)
            dest = source;
        else if(source != null)
            for(int i=0 ; i<source.size();i++){
                int key = source.keyAt(i);
                E value = source.valueAt(i);
                dest.put(key, value);
            }
        return dest;
    }
}
