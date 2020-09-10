package com.vwm.audioutils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Xuefeng Ding
 * Created 2020-08-04 16:24
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    public static String copyFile(Context context, String assetFileName) {
        Log.d(TAG, "start copy file " + assetFileName);
        File file = context.getFilesDir();

        String tmpFile = file.getAbsolutePath() + "/" + assetFileName;
        File f = new File(tmpFile);
        //todo check md5
//        if (f.exists()) {
//            Log.d(TAG, "file exists " + assetFileName);
//            return f.getAbsolutePath();
//        }

        try (OutputStream myOutput = new FileOutputStream(f);
             InputStream myInput = context.getAssets().open(assetFileName)) {
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            Log.d(TAG, "Copy task successful");
        } catch (Exception e) {
            Log.e(TAG, "copyFile: Failed to copy", e);
        } finally {
            Log.d(TAG, "end copy file " + assetFileName);
        }
        return f.getAbsolutePath();
    }
}
