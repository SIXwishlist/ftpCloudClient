package com.sukinsan.cloudftp.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Created by victor on 1/8/2018.
 */

public class SystemUtilsImpl implements SystemUtils {

    private Context context;

    public SystemUtilsImpl(Context context) {
        this.context = context;
    }

    @Override
    public void exec(File file) {
        String filename = file.getName().toLowerCase();

        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (filename.contains(".doc") || filename.contains(".docx")) {
            intent.setDataAndType(uri, "application/msword");
        } else if (filename.contains(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");
        } else if (filename.contains(".ppt") || filename.contains(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (filename.contains(".xls") || filename.contains(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (filename.contains(".zip") || filename.contains(".rar")) {
            intent.setDataAndType(uri, "application/zip");
        } else if (filename.contains(".rtf")) {
            intent.setDataAndType(uri, "application/rtf");
        } else if (filename.contains(".wav") || filename.contains(".mp3")) {
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (filename.contains(".gif")) {
            intent.setDataAndType(uri, "image/gif");
        } else if (filename.contains(".jpg") || filename.contains(".jpeg") || filename.contains(".png")) {
            intent.setDataAndType(uri, "image/jpeg");
        } else if (filename.contains(".txt")) {
            intent.setDataAndType(uri, "text/plain");
        } else if (filename.contains(".3gp") || filename.contains(".mpg") || filename.contains(".mpeg") || filename.contains(".mpe") || filename.contains(".mp4") || filename.contains(".avi")) {
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
