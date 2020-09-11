package com.vwm.audioutils;

/**
 * @author Xuefeng Ding
 * Created 2020-02-17 23:49
 */
public class RingBufferType2 extends RingBuffer {
    int recordingOffset = 0;
    float[] inputBuffer;

    public RingBufferType2(int length) {
        super(length);//构造函数定义缓冲区的大小
        inputBuffer = new float[length];
    }

    public boolean put(float[] audioBuffer) {
        int maxLength = buf.length;
        int newRecordingOffset = recordingOffset + audioBuffer.length;
        int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
        int firstCopyLength = audioBuffer.length - secondCopyLength;
        // We store off all the data for the recognition thread to access. The ML
        // thread will copy out of this buffer into its own, while holding the
        // lock, so this should be thread safe.
        lock.writeLock().lock();
        try {
            System.arraycopy(audioBuffer, 0, buf, recordingOffset, firstCopyLength);
            System.arraycopy(audioBuffer, firstCopyLength, buf, 0, secondCopyLength);
            recordingOffset = newRecordingOffset % maxLength;
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    public float[] get() {
        lock.readLock().lock();
        try {
            int maxLength = buf.length;
            int firstCopyLength = maxLength - recordingOffset;
            int secondCopyLength = recordingOffset;
            System.arraycopy(buf, recordingOffset, inputBuffer, 0, firstCopyLength);
            System.arraycopy(buf, 0, inputBuffer, firstCopyLength, secondCopyLength);
        } finally {
            lock.readLock().unlock();
        }
        return inputBuffer;
    }
}
