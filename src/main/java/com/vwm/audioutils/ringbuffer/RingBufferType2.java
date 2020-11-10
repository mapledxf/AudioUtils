package com.vwm.audioutils.ringbuffer;

/**
 * @author Xuefeng Ding
 * Created 2020-02-17 23:49
 */
class RingBufferType2 extends BaseRingBuffer {
    private int recordingOffset = 0;
    private float[] inputBuffer;

    public RingBufferType2(int length) {
        super(length);//构造函数定义缓冲区的大小
        inputBuffer = new float[length];
    }

    /**
     * write a segment to buffer
     *
     * @param slice slice of data
     * @return true if success
     */
    @Override
    public boolean put(float[] slice) {
        // We store off all the data for the recognition thread to access. The ML
        // thread will copy out of this buffer into its own, while holding the
        // lock, so this should be thread safe.
        try {
            lock.writeLock().lock();
            int maxLength = buf.length;
            int newRecordingOffset = recordingOffset + slice.length;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = slice.length - secondCopyLength;
            System.arraycopy(slice, 0, buf, recordingOffset, firstCopyLength);
            System.arraycopy(slice, firstCopyLength, buf, 0, secondCopyLength);
            recordingOffset = newRecordingOffset % maxLength;
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    /**
     * get the whole buffer
     *
     * @return buffer
     */
    @Override
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
