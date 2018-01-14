package com.sukinsan.cloudftp.util;

import android.content.Context;

import com.sukinsan.koshcloudcore.util.MyCloudStorage;

/**
 * Created by victor on 1/14/2018.
 */

public class CloudStorageImpl implements MyCloudStorage {
    private Context context;

    public CloudStorageImpl(Context context) {
        this.context = context;
    }

    @Override
    public void set(String s, String s1) {

    }

    @Override
    public void set(String s, boolean b) {

    }

    @Override
    public String getString(String s, String s1) {
        return null;
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return false;
    }
}
