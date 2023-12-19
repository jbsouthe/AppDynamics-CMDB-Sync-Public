package com.appdynamics.cmdb;

import java.util.Map;
import java.util.TreeMap;

public class CacheManager<K,T extends IResult> {
    private Map<K,T> cacheMap = new TreeMap<>();

    public T put( K key, T result) {
        if(result == null || result.isEmpty()) return null;
        T existingResult = this.cacheMap.get(key);
        if( existingResult != null ) {
            if(existingResult.update(result))
                return result;
        } else {
            this.cacheMap.put(key, result);
            return result;
        }
        return null; //if this item is not new and was not updated
    }

    public T get(K key, int cacheValidMinutes ) {
        T result = this.cacheMap.get(key);
        if( result == null || result.isEmpty() || !result.isCacheValid(cacheValidMinutes) )
            return null;
        return result;
    }
}
