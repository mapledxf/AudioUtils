package com.vwm.audioutils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Xuefeng Ding
 * Created 2020/9/11
 */
public class AudioConverter {
    private AudioConverter() {
    }

    public static float[] bytesToFloats(byte[] input) {
        short[] shorts = bytesToShorts(input);
        return shortsToFloats(shorts);
    }

    public static short[] bytesToShorts(byte[] input) {
        short[] shorts = new short[input.length / 2];
        ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    public static float[] shortsToFloats(short[] input) {
        float[] floats = new float[input.length];
        for (int i = 0; i < input.length; ++i) {
            floats[i] = input[i] / (Short.MAX_VALUE + 1f);
        }
        return floats;
    }

    public static byte[] shortsToBytes(short[] input) {
        byte[] byteData = new byte[input.length * 2];
        ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(input);
        return byteData;
    }

    public static short[] floatArrayToShortArray(float[] audio) {
        short[] ret = new short[audio.length];
        for (int i = 0; i < audio.length; i++) {
            ret[i] = (short) (audio[i] * (Short.MAX_VALUE + 1));
        }
        return ret;
    }

    public static byte[] floatsToBytes(float[] input) {
        short[] s = floatArrayToShortArray(input);
        return shortsToBytes(s);
    }
}
