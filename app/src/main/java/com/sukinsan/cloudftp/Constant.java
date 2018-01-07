package com.sukinsan.cloudftp;

import android.os.Environment;

import java.io.File;

/**
 * Created by victor on 1/7/2018.
 */

public class Constant {
    private final static String CLOUD_FOLDER_NAME = "myCloudFtpFolder";

    public static String getCloudFolder() {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), CLOUD_FOLDER_NAME);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f.getAbsolutePath();
    }

}