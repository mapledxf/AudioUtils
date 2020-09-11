package com.vwm.audioutils;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Xuefeng Ding
 * Created 2020-02-17 23:49
 */
public abstract class RingBuffer {
    float[] buf;
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static RingBuffer createType1(int length) {
        return new RingBufferType1(length);
    }

    public static RingBuffer createType2(int length) {
        return new RingBufferType2(length);
    }

    public RingBuffer(int length) {//构造函数定义缓冲区的大小
        buf = new float[length];
    }

    public abstract boolean put(float[] ar);

    public abstract float[] get();
}
