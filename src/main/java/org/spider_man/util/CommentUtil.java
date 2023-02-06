package org.spider_man.util;

import org.apache.commons.codec.digest.Md5Crypt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentUtil {
    public static String Md5(String content) {
        return Md5Crypt.md5Crypt(content.getBytes());
    }

    public static String re(String pattern, String content) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public static List<String> reList(String pattern, String content) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        int count = m.groupCount();
        List<String> result = new ArrayList<>();
        if (m.find()) {
            for (int i = 0; i <= count; i++) {
                result.add(m.group(i));
            }
        }
        return result;
    }
}
