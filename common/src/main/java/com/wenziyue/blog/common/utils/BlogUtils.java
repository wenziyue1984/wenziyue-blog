package com.wenziyue.blog.common.utils;

import lombok.val;

/**
 * 博客工具类
 *
 * @author wenziyue
 */
public class BlogUtils {

    /**
     * 安全的trim
     */
    public static String safeTrim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * safeTrim增强，如果safeTrim()返回空字符串，则返回null
     * 防止数据库插入空字符串
     */
        public static String safeTrimEmptyIsNull(String str) {
        val trim = safeTrim(str);
        return trim == null || trim.isEmpty() ? null : trim;
    }
}
