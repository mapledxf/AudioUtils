package com.ebo.voice.audio.process.jni;

public class EcnrConnector {

    public static native void initDSP(String configPath);

    public static native void process(byte[] data,int size);

    public static native byte[] getStreamForEcnr();

    static {
        System.loadLibrary("ecnr_lib");
    }
}
