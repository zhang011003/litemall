package org.linlinjava.litemall.core.util;

import com.google.common.collect.Lists;
import com.qiniu.util.Md5;

import java.util.List;
import java.util.Map;

public class LeShuaUtil {
    public static String getSign(Map<String, String> map, String key) {
        List<String> keys = Lists.newArrayList(map.keySet());
        StringBuilder builder = map.keySet().stream().sorted(String::compareTo).collect(StringBuilder::new, (x, y) -> x.append(y).append("=").append(map.get(y)).append("&"),(x, y)-> x.append(y));
        builder.append("key=").append(key);
        return Md5.md5(builder.toString().getBytes()).toUpperCase();
    }
}
