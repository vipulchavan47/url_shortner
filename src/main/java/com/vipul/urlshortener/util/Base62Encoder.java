package com.vipul.urlshortener.util;


public class Base62Encoder {

    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = 62;

    public static String encode(Long number) {
        if (number == 0) return "a";  // edge case

        StringBuilder sb = new StringBuilder();

        while (number > 0) {
            int remainder = (int) (number % BASE);
            sb.append(CHARSET.charAt(remainder));
            number = number / BASE;
        }

        return sb.reverse().toString();
    }
}