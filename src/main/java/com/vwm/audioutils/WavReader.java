package com.vwm.audioutils;

import java.io.File;
import java.io.RandomAccessFile;


public class WavReader {
	private File file;
	private byte[] header;
	private short[] samples;
	public int maxSample;
	//this is the absolute value maximum so it considers the most negative sample aswell
	public int maxAbsoluteSample;

	public WavReader(String path) {
		this.file = new File(path);
		this.header = readFile(0, 44);
		this.samples = this.readRawData();
	}

	public int getNumOfChannels() {
		return Integer.decode(this.toLittleEndianHex(22, 23, this.header));
	}

	public int getSampleRate() {
		return Integer.decode(this.toLittleEndianHex(24, 27, this.header));
	}

	public int getBitsPerSample() {
		return Integer.decode(this.toLittleEndianHex(16, 19, this.header));
	}

	//gets the size of actual audio data which is: total file size - header bytes
	public int getRawDataSize() {
		return Integer.decode(this.toLittleEndianHex(40, 43, this.header));
	}

	public short[] getSamples() {
		return this.samples;
	}


	//normalizes sample values bounded within [-bound,bound]
	public double[] normalizeSamples(int[] samples, int bound) {
		double[] normalizedSamples = new double[samples.length];
		for (int i = 0; i < samples.length; i++) {
			normalizedSamples[i] = (double) samples[i] / ((double) this.maxAbsoluteSample / bound);
		}
		return normalizedSamples;
	}


	//PRIVATE METHODS START HERE-----------PRIVATE METHODS START HERE---------------------------------------
	//***reads raw/audio bytes decodes the sample value and stores them into an integer array***
	private short[] readRawData() {
		int numOfRawDataBytes = this.getRawDataSize();
		int bitsPerSample = this.getBitsPerSample();
		int bytesPerSample = bitsPerSample / 8;
		byte[] data = readFile(44, numOfRawDataBytes);
		int numOfSamples = this.getNumOfSamples();
		short[] samples = new short[numOfSamples];

		int samplesIndex = 0;
		int j;
		int max = 0;
		int absoluteMax = 0;

		//read  bytesPerSample at a time and store as integer
		for (int i = 0; i < data.length - 1; i += bytesPerSample) {
			j = i + 1;
			samples[samplesIndex] = this.toSignedLittleEndianShort(i, j, data);

			if (max < samples[samplesIndex]) {
				max = samples[samplesIndex];
			}
			if (absoluteMax < Math.abs(samples[samplesIndex])) {
				absoluteMax = Math.abs(samples[samplesIndex]);
			}
			this.maxSample = max;
			this.maxAbsoluteSample = absoluteMax;
			samplesIndex++;
		}
		return samples;
	}


	private int getNumOfSamples() {
		return this.getRawDataSize() / (this.getNumOfChannels() * (this.getBitsPerSample() / 8));
	}

	//***helper method converts byte order to Little Endian and returns a hex string***
	private String toLittleEndianHex(int start, int end, byte[] data) {
		StringBuilder str = new StringBuilder("0x");
		for (int i = end; i >= start; i--) {
			str.append(String.format("%02x", data[i]));
		}
		return str.toString();
	}

	//***helper method converts a series of bytes into signed integer values in little endian byte order***
	private short toSignedLittleEndianShort(int start, int end, byte[] data) {
		StringBuilder str = new StringBuilder();
		for (int i = end; i >= start; i--) {
			str.append(String.format("%02x", data[i]));
		}
		return (short) Integer.parseInt(str.toString(), (end - start + 1) * 8);

	}

	//****offset is the byte in the file you want to start reading from****
	private byte[] readFile(long offset, int numBytes) {
		byte[] data = new byte[numBytes];
		try (RandomAccessFile randomAccess = new RandomAccessFile(this.file, "r")) {
			randomAccess.seek(offset);
			randomAccess.read(data, 0, numBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

}