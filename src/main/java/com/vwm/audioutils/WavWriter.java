package com.vwm.audioutils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Xuefeng Ding
 * Created 2020-02-26 12:50
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class WavWriter {
    private static final int MAX_WAV_FILES = 100;
    private final int sampleRate;

    private BufferedOutputStream os;
    private File pcmFile;

    /**
     * Create a wav writer.
     * @param path path to save to data
     * @param uuid file name
     * @param sampleRate sample rate
     */
    public WavWriter(String path, String uuid, int sampleRate) {
        this.sampleRate = sampleRate;
        try {
            File dir = new File(path);
            dir.mkdirs();
            cleanOldFiles(dir);

            pcmFile = new File(dir, uuid + ".pcm");
            pcmFile.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(pcmFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanOldFiles(File wavDir) {
        File[] logFiles = wavDir.listFiles((dir, name) ->
                "wav".equalsIgnoreCase(name.substring(name.lastIndexOf(".") + 1)));
        long oldestDate = Long.MAX_VALUE;
        File oldestFile = null;
        if (logFiles != null && logFiles.length > MAX_WAV_FILES) {
            //delete oldest files after theres more than 500 log files
            for (File f : logFiles) {
                if (f.lastModified() < oldestDate) {
                    oldestDate = f.lastModified();
                    oldestFile = f;
                }
            }

            if (oldestFile != null) {
                oldestFile.delete();
            }
        }
    }

    /**
     * write shorts data into the file
     * @param shorts short data
     */
    public void writePCM(short[] shorts) {
        writePCM(AudioConverter.shortsToBytes(shorts));
    }

    /**
     * write bytes data into the file
     * @param bytes byte data
     */
    public void writePCM(byte[] bytes) {
        writePCM(bytes, bytes.length);
    }

    public void writePCM(byte[] bytes, int length) {
        if (os != null) {
            try {
                os.write(bytes, 0, length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveToWav() {
        if (os != null) {
            try {
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        convertWaveFile(pcmFile.getAbsolutePath(), pcmFile.getAbsolutePath() + ".wav");
        pcmFile.delete();
    }


    private void convertWaveFile(String pcm, String wav) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        int channels = 1;
        long byteRate = 16 * sampleRate * channels / 8;
        byte[] data = new byte[512];
        try {
            in = new FileInputStream(pcm);
            out = new FileOutputStream(wav);
            totalAudioLen = in.getChannel().size();
            //由于不包括RIFF和WAV
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(out, totalAudioLen, totalDataLen, (long) sampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}
