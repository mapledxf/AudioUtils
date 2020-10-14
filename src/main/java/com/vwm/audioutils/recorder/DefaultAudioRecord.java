package com.vwm.audioutils.recorder;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.util.Log;

/**
 * Author Xuefeng Ding, Email xfding@vw-mobvoi.com
 * <p>
 * created 2019/4/19 15:27
 */
class DefaultAudioRecord extends BaseAudioRecord {

    private NoiseSuppressor mNoiseSuppressor;
    private AutomaticGainControl mAutomaticGainControl;
    private AcousticEchoCanceler mAcousticEchoCanceler;

    DefaultAudioRecord(int sampleRate, boolean dump) {
        super(sampleRate);
    }

    @Override
    protected void onAudioDataReceived(byte[] buffer, int numOfBytes) {
        AudioRecordManager.getInstance().dispatch(buffer, numOfBytes);
    }

    @Override
    protected AudioRecord createAudioRecord() {
        int bufferSize = getBufferSize();
        AudioRecord ar = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate, CHANNEL, AUDIO_FORMAT, bufferSize * 2);
        if (ar.getState() == AudioRecord.STATE_INITIALIZED) {
            Log.d(TAG, "new AudioRecord SampleRate : " + sampleRate + ", BufferSize : " + bufferSize);
            setEffect(ar.getAudioSessionId());
            return ar;
        } else {
            ar.release();
            Log.e(TAG, "AudioRecord failed to initialize: " + ar.getState());
            return null;
        }
    }

    private void setEffect(int audioSessionId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Log.i(TAG, "Trying to enhance audio because running on SDK " + Build.VERSION.SDK_INT);

            if (NoiseSuppressor.isAvailable()) {
                mNoiseSuppressor = NoiseSuppressor.create(audioSessionId);

                if (!mNoiseSuppressor.getEnabled()) {
                    mNoiseSuppressor.setEnabled(true);
                }
                Log.d(TAG, "NoiseSuppressor status: " + mNoiseSuppressor.getEnabled());
            } else {
                Log.e(TAG, "NoiseSuppressor not support");
            }

            if (AutomaticGainControl.isAvailable()) {
                mAutomaticGainControl = AutomaticGainControl.create(audioSessionId);

                if (!mAutomaticGainControl.getEnabled()) {
                    mAutomaticGainControl.setEnabled(true);
                }
                Log.d(TAG, "AutomaticGainControl status: " + mAutomaticGainControl.getEnabled());
            } else {
                Log.e(TAG, "AutomaticGainControl not support");
            }

            if (AcousticEchoCanceler.isAvailable()) {
                mAcousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId);

                if (!mAcousticEchoCanceler.getEnabled()) {
                    mAcousticEchoCanceler.setEnabled(true);
                }
                Log.d(TAG, "AcousticEchoCanceler status: " + mAcousticEchoCanceler.getEnabled());
            } else {
                Log.e(TAG, "AcousticEchoCanceler not support");
            }
        }
    }

    @Override
    public void stopRecording() {
        super.stopRecording();

        if (mNoiseSuppressor != null) {
            mNoiseSuppressor.release();
            mNoiseSuppressor = null;
        }
        if (mAutomaticGainControl != null) {
            mAutomaticGainControl.release();
            mAutomaticGainControl = null;
        }
        if (mAcousticEchoCanceler != null) {
            mAcousticEchoCanceler.release();
            mAcousticEchoCanceler = null;
        }

    }

}
