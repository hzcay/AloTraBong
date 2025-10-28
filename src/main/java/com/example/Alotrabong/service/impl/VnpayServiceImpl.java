package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.config.VnpayConfig;
import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayServiceImpl implements VnpayService {

    private final VnpayConfig vnpayConfig;
    private final HttpServletRequest request;

    /** Tạo URL redirect sang trang thanh toán VNPAY */
    @Override
    public String createPaymentUrl(Order order) {
        // 1) Validate
        BigDecimal total = (order != null) ? order.getTotalAmount() : null;
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Order totalAmount is invalid");
        }

        // 2) Base params (không include vnp_SecureHash/Type ở bước build hash)
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());

        // 3) Amount x100 (không dấu, không phân cách)
        long amount = total.movePointRight(2).longValueExact();
        params.put("vnp_Amount", String.valueOf(amount));

        // 4) TxnRef: unique, bỏ dấu '-'
        String txnRef = order.getOrderId().replace("-", "");
        params.put("vnp_TxnRef", txnRef);

        // 5) OrderInfo: không dấu & không ký tự đặc biệt
        params.put("vnp_OrderInfo", "ThanhToan_" + txnRef);

        // 6) Thời gian GMT+7
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        params.put("vnp_CreateDate", fmt.format(cal.getTime()));
        cal.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", fmt.format(cal.getTime()));

        // 7) IP khách (map ::1 -> 127.0.0.1)
        params.put("vnp_IpAddr", clientIp(request));

        // (Tùy chọn) ép kênh thanh toán:
        // params.put("vnp_BankCode", "VNBANK");  // ATM nội địa
        // params.put("vnp_BankCode", "INTCARD"); // thẻ quốc tế
        // params.put("vnp_BankCode", "VNPAYQR"); // QR

        // 8) Build hashData (US-ASCII) & query (UTF-8) theo key sort ASC
        String hashData = buildPair(params, true);
        String query    = buildPair(params, false);

        // 9) Ký HMAC → append vào query
        String secureHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData);
        String url = vnpayConfig.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;

        // Log dev (tắt khi prod)
        System.out.println("VNPAY hashData(REQ): " + hashData);
        System.out.println("VNPAY secureHash(REQ): " + secureHash);
        System.out.println("VNPAY URL: " + url);

        return url;
    }

    /** Verify kết quả khi browser redirect về từ VNPAY (returnUrl) */
    @Override
    public boolean handleReturn(Map<String, String> params) {
        if (params == null || params.isEmpty()) return false;

        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) return false;

        // Copy & loại trừ các field không ký
        Map<String, String> signData = new TreeMap<>(String::compareTo);
        signData.putAll(params);
        signData.remove("vnp_SecureHash");
        signData.remove("vnp_SecureHashType");

        // Build hashData (US-ASCII) giống hệt lúc request
        String hashData = buildPair(signData, true);
        String myHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData);

        // Log dev
        System.out.println("VNPAY hashData(RET): " + hashData);
        System.out.println("VNPAY secureHash(RET.mine): " + myHash);
        System.out.println("VNPAY secureHash(RET.vnp):  " + vnpSecureHash);

        if (!myHash.equalsIgnoreCase(vnpSecureHash)) {
            System.out.println("VNPAY -> HASH MISMATCH");
            return false;
        }

        // Thành công khi ResponseCode = "00"
        return "00".equals(params.get("vnp_ResponseCode"));
    }

    // ========= Helpers =========

    /** Ghép key=value theo key sort ASC; ascii=true → US-ASCII (hash), false → UTF-8 (query) */
    private static String buildPair(Map<String, String> map, boolean ascii) {
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (String k : keys) {
            String v = map.get(k);
            if (v == null || v.isEmpty()) continue;
            if (n++ > 0) sb.append('&');
            if (ascii) {
                sb.append(URLEncoder.encode(k, StandardCharsets.US_ASCII))
                  .append('=')
                  .append(URLEncoder.encode(v, StandardCharsets.US_ASCII));
            } else {
                sb.append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                  .append('=')
                  .append(URLEncoder.encode(v, StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }

    /** HMAC-SHA512 (UTF-8) – trả "" nếu lỗi để dễ debug mà không ném exception */
    private static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) throw new NullPointerException();
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /** Lấy IP client có xét proxy headers; map ::1 -> 127.0.0.1 */
    private static String clientIp(HttpServletRequest req) {
        String[] headers = {
            "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA",
            "REMOTE_ADDR"
        };
        for (String h : headers) {
            String v = req.getHeader(h);
            if (v != null && !v.isEmpty() && !"unknown".equalsIgnoreCase(v)) {
                return v.split(",")[0].trim();
            }
        }
        String ip = req.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}