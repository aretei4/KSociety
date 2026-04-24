package com.bccommittee.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {
    private static final NumberFormat INR = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public static String format(long amount) {
        // e.g. ₹10,000
        return INR.format(amount).replace(".00", "");
    }

    public static String formatShort(long amount) {
        if (amount >= 100000) return "₹" + (amount / 100000) + "L";
        if (amount >= 1000)   return "₹" + (amount / 1000) + "K";
        return "₹" + amount;
    }
}
