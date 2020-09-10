package com.vwm.audioutils;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Xuefeng Ding
 * Created 2020-02-17 23:49
 *
 */
public class RingBuffer {
    private float[] buf;
    private int start;
    private int end;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RingBuffer(int s) {//构造函数定义缓冲区的大小
        buf = new float[s];
        this.start = this.end = 0;
    }

    public boolean put(float[] ar) {

        if (ar.length > buf.length) {
            //空间不够
            return false;
        }
        lock.writeLock().lock();
        try {
            int bufferRemain = buf.length - end;
            if (ar.length >= bufferRemain) {
//            Log.d("TESTME", "before put, start:" + start + " end:" + end + " bufferRemain:" + bufferRemain + " ar.length:" + ar.length);
                start = ar.length - bufferRemain;
                System.arraycopy(ar, 0, buf, end, bufferRemain);
                System.arraycopy(ar, bufferRemain, buf, 0, start);
            } else {
                System.arraycopy(ar, 0, buf, end, ar.length);
            }
            end = (end + ar.length) % buf.length;
//        Log.d("TESTME", "after put, start:" + start + " end:" + end);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

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
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return arr;
    }
}
