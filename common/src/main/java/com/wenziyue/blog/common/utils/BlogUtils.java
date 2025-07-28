package com.wenziyue.blog.common.utils;

import com.google.common.hash.Hashing;
import lombok.val;

import java.nio.charset.StandardCharsets;

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

    /**
     * 使用 Murmur3_128 计算指纹，取低 64 位作为最终输出，避免 32 位十六进制过长。
     */
    public static String fp(String... parts) {
        String raw = String.join("|", parts);
        long hash64 = Hashing.murmur3_128()
                .hashString(raw, StandardCharsets.UTF_8)
                .asLong();
        return Long.toUnsignedString(hash64);
    }

    /**
     * 使用 Murmur3_128 计算指纹，返回 32 字节十六进制
     * 但一般不需要这么长。
     */
    public static String fpHex128(String... parts) {
        String raw = String.join("|", parts);
        return Hashing.murmur3_128()
                .hashString(raw, StandardCharsets.UTF_8)
                .toString(); // 32个hex字符
    }

}
