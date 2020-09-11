package com.vwm.audioutils.recorder;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import com.vwm.commonutils.ThreadPoolManager;
import com.vwm.commonutils.permission.PermissionHelper;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Xuefeng Ding
 * Created 2020/9/11
 */
public class AudioRecordManager {
    private static final String TAG = "AudioRecordManager";
    private static volatile AudioRecordManager instance;
    private static final Object INSTANCE_WRITE_LOCK = new Object();

    public static AudioRecordManager getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_WRITE_LOCK) {
                if (instance == null) {
                    instance = new AudioRecordManager();
                }
            }
        }
        return instance;
    }

    protected CopyOnWriteArrayList<AudioDataListener> mListeners = new CopyOnWriteArrayList<>();
    private BaseAudioRecord mRecorder;

    /**
     * init the recorder and start to record.
     * @param context context
     * @param sampleRate desired sample rate
     */
    public void init(Context context, int sampleRate) {
        PermissionHelper permissionHelper = new PermissionHelper(granted -> {
            if (granted) {
                onReady(sampleRate);
            }
        });

        permissionHelper.checkPermissions(context.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO);
    }

    private void onReady(int sampleRate) {
        Log.d(TAG, "onReady: ");
        mRecorder = BaseAudioRecord.createAudioRecorder(sampleRate);
        startRecord();
    }

    private void startRecord() {
        Log.d(TAG, "startRecord: ");
        mRecorder.startRecording();
    }

    private void stopRecord() {
        Log.d(TAG, "stopRecord: ");
        mRecorder.stopRecording();
    }

    public void release() {
        Log.d(TAG, "release: ");
        stopRecord();
        mListeners.clear();
    }

    /**
     * add a audio data listener
     * @param listener AudioDataListener
     */
    public void addListener(AudioDataListener listener) {
        if (mListeners.contains(listener)) {
            return;
        }
        Log.d(TAG, "addListener: " + listener.getClass());
        mListeners.add(listener);
    }

    /**
     * remove the listener
     * @param listener AudioDataListener
     */
    public void removeListener(AudioDataListener listener) {
        if (mListeners.contains(listener)) {
            Log.d(TAG, "removeListener: " + listener.getClass());
            mListeners.remove(listener);
        }
    }

    /**
     * Receive the bytes data from recorder and dispatch to listeners.
     * @param audioData the audio bytes from recorder
     * @param numOfBytes the number of bytes read from recorder
     */
    protected void dispatch(byte[] audioData, int numOfBytes) {
        for (AudioDataListener listener : mListeners) {
            ThreadPoolManager.getInstance().execute(() ->
                    listener.onAudioData(audioData, numOfBytes));
        }
    }
}
