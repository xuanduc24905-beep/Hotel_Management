package com.lotuslaverne.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Cost factor 12 ≈ ~300ms/hash trên phần cứng thông thường — đủ chậm để chống brute-force
    private static final int COST = 12;

    /** Băm mật khẩu thuần thành BCrypt hash (đã chứa salt ngẫu nhiên bên trong). */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
    }

    /**
     * So sánh mật khẩu người dùng nhập với hash đã lưu trong DB.
     * Trả về false ngay nếu hash null hoặc không phải định dạng BCrypt.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$2")) return false;
        return BCrypt.checkpw(plainPassword, storedHash);
    }
}
