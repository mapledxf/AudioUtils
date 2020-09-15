package com.vwm.audioutils.recorder;

import android.content.Context;

import com.ebo.voice.audio.process.jni.EcnrConnector;
import com.vwm.commonutils.FileUtils;

import java.io.File;

public class JettaEcnrProcess {
    private static final String DSP_WAKEUP_PATH = "wake_up";

    public JettaEcnrProcess(Context context) {
        String path = FileUtils.copyFile(context, "dsp_config.zip");
        String dspConfig = path + File.separator + DSP_WAKEUP_PATH;
        EcnrConnector.initDSP(dspConfig);
    }

    public byte[] process(byte[] audioData, int numOfBytes) {
        EcnrConnector.process(audioData, numOfBytes);
        return EcnrConnector.getStreamForEcnr();
    }
}
