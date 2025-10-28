package com.example.Alotrabong.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class VndFormatter {

    private static final Locale VN_LOCALE = new Locale("vi", "VN");

    public String format(BigDecimal amount) {
        if (amount == null) return "0 đ";
        NumberFormat nf = NumberFormat.getInstance(VN_LOCALE); // 12.345, 99...
        return nf.format(amount) + " đ";
    }
}
