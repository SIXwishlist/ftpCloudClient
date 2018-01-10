package com.sukinsan.cloudftp.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * Created by victor on 1/8/2018.
 */

public class SystemUtilsImpl implements SystemUtils {

    private static final String TAG = SystemUtils.class.getSimpleName();
    private Context context;

    public SystemUtilsImpl(Context context) {
        this.context = context;
    }

    @Override
    public void exec(File file) {
        Uri uri = getUri(file);

        context.startActivity(new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, getFileMimeType(file))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
    }

    @Override
    public void share(File file) {
        Uri uri = getUri(file);

        context.startActivity(new Intent(Intent.ACTION_SEND)
                .setDataAndType(uri, getFileMimeType(file)));
    }

    @Override
    public Uri getUri(File file) {
        String packageNameProvider = context.getPackageName() + ".fileprovider";
        Log.i(TAG, "packageNameProvider " + packageNameProvider);
        return FileProvider.getUriForFile(context, packageNameProvider, file);
    }

    @Override
    public String getFileMimeType(File file) {
        String filename = file.getName();

        if (filename.contains(".doc") || filename.contains(".docx")) {
            return "application/msword";
        } else if (filename.contains(".pdf")) {
            return "application/pdf";
        } else if (filename.contains(".ppt") || filename.contains(".pptx")) {
            return "application/vnd.ms-powerpoint";
        } else if (filename.contains(".xls") || filename.contains(".xlsx")) {
            return "application/vnd.ms-excel";
        } else if (filename.contains(".zip") || filename.contains(".rar")) {
            return "application/zip";
        } else if (filename.contains(".rtf")) {
            return "application/rtf";
        } else if (filename.contains(".wav") || filename.contains(".mp3")) {
            return "audio/x-wav";
        } else if (filename.contains(".gif")) {
            return "image/gif";
        } else if (filename.contains(".jpg") || filename.contains(".jpeg") || filename.contains(".png")) {
            return "image/jpeg";
        } else if (filename.contains(".txt")) {
            return "text/plain";
        } else if (filename.contains(".3gp") || filename.contains(".mpg") || filename.contains(".mpeg") || filename.contains(".mpe") || filename.contains(".mp4") || filename.contains(".avi")) {
            return "video/*";
        } else {
            return "*/*";
        }
    }

}