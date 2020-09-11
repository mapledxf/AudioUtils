package com.vwm.audioutils.recorder;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

/**
 * Author Xuefeng Ding, Email xfding@vw-mobvoi.com
 * <p>
 * created 2019/4/19 15:27
 */
class JettaAudioRecord extends BaseAudioRecord {

    JettaAudioRecord(int sampleRate) {
        super(sampleRate);
    }

    @Override
    protected AudioRecord createAudioRecord(int bufferSize) {
        AudioRecord ar = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                1020,
                AUDIO_FORMAT,
                bufferSize);
        if (ar.getState() == AudioRecord.STATE_INITIALIZED) {
            Log.d(TAG, "new AudioRecord SampleRate : " + sampleRate + ", BufferSize : " + bufferSize);
            return ar;
        } else {
            ar.release();
            Log.e(TAG, "AudioRecord failed to initialize: " + ar.getState());
            return null;
        }
    }

    @Override
    int read(byte[] buffer) throws IOException {
        byte[] raw = new byte[buffer.length];
        int length = super.read(raw);
        return split(raw, length, buffer);
    }

    private int split(byte[] raw, int length, byte[] buff) {
        int out = 0;
        int in = 0;
        while (in < length) {
            buff[out] = raw[in + 1];
            in += 8;
            out++;
        }
        return out;
    }

    @Override
    int getBufferSize() {
        return super.getBufferSize() * 8;
    }
}
