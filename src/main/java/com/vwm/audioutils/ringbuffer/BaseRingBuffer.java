package com.vwm.audioutils.ringbuffer;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Xuefeng Ding
 * Created 2020-02-17 23:49
 */
public abstract class BaseRingBuffer {
    static volatile float[] buf;
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * fill the buffer from beginning
     * @param length buffer size
     * @return RingBufferType1
     */
    public static BaseRingBuffer createType1(int length) {
        return new RingBufferType1(length);
    }

    /**
     * fill the buffer from end
     * @param length buffer size
     * @return RingBufferType2
     */
    public static BaseRingBuffer createType2(int length) {
        return new RingBufferType2(length);
    }

    BaseRingBuffer(int length) {//构造函数定义缓冲区的大小
        buf = new float[length];
    }

    /**
     * write a segment to buffer
     * @param slice slice of data
     * @return true if success
     */
    public abstract boolean put(float[] slice);

    /**
     * get the whole buffer
     * @return buffer
     */
    public abstract float[] get();

}
