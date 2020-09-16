package com.vwm.audioutils.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.vwm.audioutils.AudioWriter;

public class JettaAudioRecord extends BaseAudioRecord {
    private static final String ACTION_NEED_TRIGGER_RECORDER = "com.ticauto.voiceengine.TRIGGER_RECORDER";

    private Context mContext;
    private AudioManager.OnMicFocusChangeListener mListener;
    private MicLocalBroadcastReceiver mReceiver;
    private JettaEcnrProcess ecnrProcess;
    private static final int RECORDER_BUFFER_SIZE = 256000;
    private static final int AEC_FRAME_LEN = 10;
    private final int AEC_FRAME_SIZE;

    private AudioWriter oriWriter;
    private AudioWriter ecnrWriter;

    public JettaAudioRecord(Context context, int sampleRate, boolean dump) {
        super(sampleRate);

        if (dump) {
            oriWriter = new AudioWriter("/sdcard/dump", "ori", 16000);
            ecnrWriter = new AudioWriter("/sdcard/dump", "ecnr", 16000);
        }
        mContext = context;
        this.sampleRate = sampleRate;
        AEC_FRAME_SIZE = sampleRate * AEC_FRAME_LEN * 2 * 8 / 1000;
        this.ecnrProcess = new JettaEcnrProcess(context);

        mListener = new MicFocusChangeListener();
        mReceiver = new MicLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEED_TRIGGER_RECORDER);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
    }

    @Override
    public void startRecording() {
        Log.d(TAG, "startRecording");
        requestMicFocus();
//        super.startRecording();
    }

    @Override
    protected void onAudioDataReceived(byte[] buffer, int numOfBytes) {
        if (oriWriter != null) {
            oriWriter.writePCM(buffer);
        }
        byte[] ecnr = ecnrProcess.process(buffer, numOfBytes);
        if (ecnrWriter != null) {
            ecnrWriter.writePCM(ecnr);
        }
        AudioRecordManager.getInstance().dispatch(ecnr, ecnr.length);
    }

    @Override
    public void stopRecording() {
//        super.stopRecording();
        Log.d(TAG, "stopRecord");
        releaseMicFocus();
    }

    public void requestMicFocus() {
        releaseMicFocus();
        Log.d(TAG, "requestMicFocus");
        final AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestMicFocus(mListener, AudioManager.Audio_SDS);

    }

    public void releaseMicFocus() {
        Log.d(TAG, "releaseMicFocus");
        final AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.MicFocusDestroyed(mListener, AudioManager.Audio_SDS);
    }

    @Override
    public AudioRecord createAudioRecord() {
        AudioRecord ar = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, 1020, AudioFormat.ENCODING_PCM_16BIT,
                RECORDER_BUFFER_SIZE);
        if (ar.getState() == AudioRecord.STATE_INITIALIZED) {
            Log.d(TAG, "new AudioRecord SampleRate : " + sampleRate + ", BufferSize :" + RECORDER_BUFFER_SIZE);
            return ar;
        } else {
            ar.release();
            Log.e(TAG, "AudioRecord failed to initialize");
            return null;
        }
    }

    private class MicFocusChangeListener implements AudioManager.OnMicFocusChangeListener {

        @Override
        public void release() {
            Log.i(TAG, "AudioMicListener  release DONE ");
        }

        /**
         * Mic focus change callback
         *
         * @param focusChange 1 AudioManager.MIC_REQ_GAIN: Apply focus to success or regain focus
         *                    2 AudioManager.MIC_REQ_FAILED: Application focus failed
         *                    3 AudioManager.MIC_REQ_LOSS: Lose focus
         */
        @Override
        public void onMicFocusChange(int focusChange) {
            Log.i(TAG, "onMicFocusChange focusChange:" + focusChange + ", mIsNeedRecording:" + mIsFinished);
            switch (focusChange) {
                case AudioManager.MIC_REQ_GAIN:
                    JettaAudioRecord.super.startRecording();
                    break;
                case AudioManager.MIC_REQ_FAILED:
                case AudioManager.MIC_REQ_LOSS:
                default:
                    JettaAudioRecord.super.stopRecording();
                    Log.e(TAG, "onMicFocusChange - current SDS no mic focus, stop record");
                    break;
            }
        }
    }

    private class MicLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive - intent : " + intent + ", mIsFinished : " + mIsFinished + ", mStarted : " + mStarted);
            if (ACTION_NEED_TRIGGER_RECORDER.equals(intent.getAction())) {
                if (!mIsFinished) {
                    if (mStarted) {
                        requestMicFocus();
                    }
                } else {
                    Log.e(TAG, "ACTION_NEED_TRIGGER_RECORDER - user trigger voice but record stopped by other app");
                }
            }
        }
    }

    @Override
    int getBufferSize() {
        return AEC_FRAME_SIZE;
    }
}
