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
     * Chấp nhận cả BCrypt hash lẫn plain-text (dành cho tài khoản seed/demo
     * chưa được băm).
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) return false;
        if (storedHash.startsWith("$2")) {
            return BCrypt.checkpw(plainPassword, storedHash);
        }
        // Fallback: so sánh plain-text (chỉ xảy ra với dữ liệu seed chưa băm)
        return plainPassword.equals(storedHash);
    }
}
