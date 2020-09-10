package com.vwm.audioutils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioConverter {
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
}
