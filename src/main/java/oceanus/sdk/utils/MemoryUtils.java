package oceanus.sdk.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class MemoryUtils {
    static void fillToStringIntoMap(Map<String, Object> targetMap, Map<String, Object> toStringSourceMap, String keyName) {
        Set<String> keys = toStringSourceMap.keySet();
        Map<String, Object> map = new HashMap<>();
        for(String key : keys) {
            map.put(key, toStringSourceMap.get(key));
        }
        targetMap.put(keyName, map);
    }
    static void fillMemoryIntoMap(Map<String, Object> targetMap, Map<String, Object> toStringSourceMap, String keyName) {
        Set<String> keys = toStringSourceMap.keySet();
        Map<String, Object> map = new HashMap<>();
        for(Object key : keys) {
            map.put(key, toStringSourceMap.get(key).memory());
        }
        targetMap.put(keyName, map)
    }

}