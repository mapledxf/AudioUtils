package com.vwm.audioutils.recorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Build;
import android.util.Log;

import com.vwm.commonutils.ThreadPoolManager;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * @author Xuefeng Ding
 * Created 2020-02-19 10:35
 */
public abstract class BaseAudioRecord {
    final String TAG = getClass().getSimpleName();
    private AudioRecord mAudioRecord;

    static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    int sampleRate;
    boolean mStarted;
    volatile boolean mIsFinished = true;
    private final Executor mExecutor = ThreadPoolManager.getInstance().getSingleExecutor(TAG, Thread.MAX_PRIORITY);

    BaseAudioRecord(int sampleRate) {
        mStarted = false;
        this.sampleRate = sampleRate;
    }

    /**
     * create AudioRecorder base on the device
     *
     * @param sampleRate record sample rate
     * @return recorder
     */
    public static BaseAudioRecord createAudioRecorder(Context context, int sampleRate, boolean dump) {
        if ("Bosch".equalsIgnoreCase(Build.MANUFACTURER)) {
            return new JettaAudioRecord(context, sampleRate, dump);
        } else if ("freescale".equals(Build.MANUFACTURER)) {
//            return new NxpAudioRecord();
            return new DefaultAudioRecord(sampleRate, dump);
        } else {
            return new DefaultAudioRecord(sampleRate, dump);
        }
    }

    /**
     * start record voice
     */
    public void startRecording() {
        if (!mIsFinished && mAudioRecord != null) {
            return;
        }
        mIsFinished = false;

        mAudioRecord = createAudioRecord();
        mExecutor.execute(() -> {
            Log.d(TAG, "startRecording");
            try {
                byte[] buffer = new byte[getBufferSize()/2];
                while (!mIsFinished) {
                    int numOfBytes = read(buffer);
                    if (numOfBytes > 0) {
                        onAudioDataReceived(buffer, numOfBytes);
//                        wavWriter.write(buffer, numOfBytes);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "startRecording: ", e);
            }
        });
    }

    protected abstract void onAudioDataReceived(byte[] buffer, int numOfBytes);

    protected abstract AudioRecord createAudioRecord();


    /**
     * release the recorder
     */
    public void stopRecording() {
        mIsFinished = true;
        mStarted = false;

        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    int getBufferSize() {
//        int minBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, CHANNEL, AUDIO_FORMAT);
//        if (minBufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE || minBufferSizeInBytes == AudioRecord.ERROR) {
//            throw new IllegalArgumentException("SpeechRecord.getMinBufferSize: parameters not supported by hardware");
//        }
//        int bufferSize = BUFFER_SIZE_MULTIPLIER * minBufferSizeInBytes;
//        Log.i(TAG, "SpeechRecord buffer size: " + bufferSize + ", min size = " + minBufferSizeInBytes);
//        return 2560;
        return Math.round((float)this.sampleRate * 0.4F);
    }

    private AudioRecord ensureStartedLocked() throws IOException {
        if (mAudioRecord == null) {
            throw new IOException("AudioRecord failed to initialize.");
        } else if (mStarted) {
            return mAudioRecord;
        } else {
            mAudioRecord.startRecording();
            int recordingState = mAudioRecord.getRecordingState();
            if (recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                throw new IOException("couldn't start recording, state is:" + recordingState);
            }
            mStarted = true;
            return mAudioRecord;
        }
    }


    int read(byte[] b) throws IOException {
        if (mIsFinished) {
            return -1;
        }
        AudioRecord record = ensureStartedLocked();
        int rtn = record.read(b, 0, b.length);
        if (rtn == AudioRecord.ERROR_INVALID_OPERATION) {
            throw new IOException("not open");
        } else if (rtn == AudioRecord.ERROR_BAD_VALUE) {
            throw new IOException("Bad offset/length arguments for buffer");
        } else {
            return rtn;
        }
    }

}
