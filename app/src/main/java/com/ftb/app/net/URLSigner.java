package com.ftb.app.net;

import java.security.SecureRandom;
import java.util.Random;

public class URLSigner {

    private static final String symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789";
    private final Random secureRandomProvider = new SecureRandom();
    private final char[] buffer;

    public URLSigner(int length) {
        if (length < 1)
            throw new IllegalArgumentException("length < 1: " + length);
        buffer = new char[length];
    }

    public String CreateKey() {
        for (int idx = 0; idx < buffer.length; ++idx)
            buffer[idx] = symbols.charAt(secureRandomProvider.nextInt(symbols.length()));
        return new String(buffer);
    }

    public static void main(String[] args) {
        System.out.println(new URLSigner(16).CreateKey());
    }

}
