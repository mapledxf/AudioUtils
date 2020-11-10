package com.vwm.audioutils.ringbuffer;

import android.util.Log;

/**
 * @author Xuefeng Ding
 * Created 2020-02-17 23:49
 */
class RingBufferType1 extends BaseRingBuffer {
    private static final String TAG = "RingBufferType1";

    private int start;
    private int end;

    public RingBufferType1(int length) {
        super(length);//构造函数定义缓冲区的大小
        this.start = this.end = 0;
    }

    /**
     * write a segment to buffer
     * @param slice slice of data
     * @return true if success
     */
    @Override
    public boolean put(float[] slice) {
        if (slice.length > buf.length) {
            //空间不够
            return false;
        }
        lock.writeLock().lock();
        try {
            int bufferRemain = buf.length - end;
            if (slice.length >= bufferRemain) {
                start = slice.length - bufferRemain;
                System.arraycopy(slice, 0, buf, end, bufferRemain);
                System.arraycopy(slice, bufferRemain, buf, 0, start);
            } else {
                System.arraycopy(slice, 0, buf, end, slice.length);
            }
            end = (end + slice.length) % buf.length;
        } catch (Exception e) {
            Log.e(TAG, "put: ", e);
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    /**
     * get the whole buffer
     * @return buffer
     */
    @Override
    public float[] get() {
        lock.readLock().lock();
        int len = buf.length;
        float[] arr = new float[len];
        try {
            System.arraycopy(buf, start, arr, 0, len - start);
            if (start != 0) {
                System.arraycopy(buf, 0, arr, len - start, start);
            }
        } catch (Exception e) {
            Log.e(TAG, "get: ", e);
        } finally {
            lock.readLock().unlock();
        }
        return arr;
    }
}
