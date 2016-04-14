package com.seagate.kinetic;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomKVGenerator implements RandKVGenerator {
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private boolean reproducible = false;
    private int totalRecords;
    private int maxKeySize;
    private int maxValueSize;
    private AtomicInteger seq = new AtomicInteger(0);

    public RandomKVGenerator(boolean reproducible, int totalRecords) {
        this.reproducible = reproducible;
        this.totalRecords = totalRecords;
    }

    public RandomKVGenerator(boolean reproducible, int totalRecords,
            int maxKeySize, int maxValueSize) {
        this.reproducible = reproducible;
        this.totalRecords = totalRecords;
        this.maxKeySize = maxKeySize;
        this.maxValueSize = maxValueSize;
    }

    @Override
    public String nextKey(int keySize) {
        if (reproducible) {
            return nextReproducibleKey(keySize);
        } else {
            return nextNonreproducibleKey(keySize);
        }
    }

    @Override
    public String getValue(String key, int valueSize) {
        int vSize = valueSize == 0 ? new Random().nextInt(maxValueSize + 1)
                : valueSize;
        if (key.length() > vSize) {
            return key.substring(0, vSize);
        } else if (key.length() == vSize) {
            return key;
        } else {
            StringBuffer sb = new StringBuffer();
            int size = 0;
            while (size < vSize) {
                sb.append(key);
                size += key.length();
            }

            return sb.toString().substring(0, vSize);
        }
    }

    private String nextNonreproducibleKey(int length) {
        Random random = new Random();
        int keyLen = length == 0 ? random.nextInt(maxKeySize) + 1 : length;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < keyLen; i++) {
            int number = random.nextInt(62);// [0,62)
            sb.append(CHARSET.charAt(number));
        }
        return sb.toString();
    }

    private String nextReproducibleKey(int length) {
        StringBuffer sb = new StringBuffer();
        int currentSeq = seq.incrementAndGet();
        int keyLen = length == 0 ? currentSeq % maxKeySize : length;
        if (keyLen == 0)
            keyLen = 1;
        int j = 0;
        for (int i = 0; i < keyLen; i++) {
            j += currentSeq;
            sb.append(CHARSET.charAt(j % 62));
        }
        return sb.toString();
    }
}
