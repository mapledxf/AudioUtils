package com.vwm.audioutils.recorder;

public interface AudioDataListener {
    public void onAudioData(byte[] audioData, int numOfBytes);
}
