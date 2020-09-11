package com.vwm.audioutils.recorder;//package com.vwm.common.recorder;
//
//import android.util.Log;
//
//import com.vwm.common.Config;
//import com.vwm.dsp.EcnrJni;
//import com.vwm.dsp.resamplerate.SampleRateConverter;
//import com.vwm.tinyrec.AudioDataListener;
//import com.vwm.tinyrec.AudioRecordTask;
//import com.vwm.tinyrec.Util;
//
//import java.nio.ByteBuffer;
//
///**
// * Author Xuefeng Ding, Email xfding@vw-mobvoi.com
// * <p>
// * created 2019/4/19 15:27
// */
//public class NxpAudioRecord extends BaseAudioRecord implements AudioDataListener {
//    private static final String TAG = "NxpAudioRecord: ";
//
//    private AudioRecordTask audioRecordTask;
//    private final int mBufferSize = 320;
//    private final static int IN_SAMPLE_RATE = 48000;
//    private final static int sampleRate = 16000;
//    private final static int ALL_CHANNELS = 4;
//    private EcnrJni ecnrJni = new EcnrJni();
//    private SampleRateConverter reSampleRate = new SampleRateConverter();
//
//    private final static int PCM_SLICE_MS = 10;
//    private BufferSlice bufferSlice = new BufferSlice(IN_SAMPLE_RATE * PCM_SLICE_MS * ALL_CHANNELS / 1000);
//    private ByteBuffer inSdsByteBuffer = ByteBuffer.allocateDirect(mBufferSize);
//
//    NxpAudioRecord() {
//        audioRecordTask = new AudioRecordTask(this, Util.numSamplesOneFrame_48K);
//        ecnrJni.initEngine(Config.DSP_CONFIG_PATH);
//        startSendAudioData();
//    }
//
//    @Override
//    public void onAudioDataReceived(short[] audioData) {
//        bufferSlice.input(audioData, audioData.length, 50, audioData.length * 1000 * ALL_CHANNELS / IN_SAMPLE_RATE, (slice, stamp) -> {
//            //bufferSlice内部的切片缓存(slice)是复用的，所以需要拷贝出来防止覆盖
//            short[] sliceCopy = new short[slice.length];
//            System.arraycopy(slice, 0, sliceCopy, 0, slice.length);
//
//
//            //从固定的通道里取出数据
//            short[] shortPlayAudio = Util.getFirstChannel(sliceCopy);
//            short[] shortRecordAudio = Util.getThirdChannel(sliceCopy);
//
//            //采样率转换48000->16000
//            short[] shortEncodePlayAudio = reSampleRate.SRC(shortPlayAudio, IN_SAMPLE_RATE, sampleRate);
//            short[] shortEncodeRecordAudio = reSampleRate.SRC(shortRecordAudio, IN_SAMPLE_RATE, sampleRate);
//
//            ecnrJni.bufferFarend(shortEncodePlayAudio, 0);
//
//            short[] resultEcnr = new short[shortEncodePlayAudio.length];
//            ecnrJni.process(resultEcnr, shortEncodeRecordAudio, 0);
//
//
//            inSdsByteBuffer.clear();
//            byte[] arry = Util.shortArrayToByteArray(resultEcnr);
//            inSdsByteBuffer.put(arry);
//            dispatchVolume(arry, arry.length);
//        });
//    }
//
//    private void startSendAudioData() {
//        bufferSlice.clear();
//        if (audioRecordTask != null) {
//            Log.d(TAG, "start audio task");
//            audioRecordTask.start();
//        } else {
//            Log.e(TAG, "audioRecordTask is null");
//        }
//    }
//
//}
