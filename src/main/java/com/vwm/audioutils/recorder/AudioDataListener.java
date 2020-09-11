package com.vwm.audioutils.recorder;

/**
 * @author Xuefeng Ding
 * Created 2020/9/11
 */
public interface AudioDataListener {
    /**
     * Audio data callback
     * @param audioData bytes data
     * @param numOfBytes number of bytes
     */
    public void onAudioData(byte[] audioData, int numOfBytes);
}
