package com.sukinsan.cloudftp;

import android.os.Environment;

import java.io.File;
import java.util.Arrays;

/**
 * Created by victor on 1/7/2018.
 */

public class Constant {
    public final static String
            ROOT = "/",
            CLOUD_FOLDER_NAME = "myCloudFtpFolder";

    public static String getDownloadFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    public static String getCloudFolder() {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), CLOUD_FOLDER_NAME);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f.getAbsolutePath();
    }

    public static String getSize(long size) {
        for (String sizeName : Arrays.asList("bytes", "Kb", "Mb")) {
            if (size < 1024) {
                return size + sizeName;
            }
            size /= 1024;
        }
        return size + "Gb";
    }

}