package com.sukinsan.cloudftp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.sukinsan.koshcloudcore.util.CloudSyncUtil;
import com.sukinsan.koshcloudcore.util.CloudSyncUtilImpl;
import com.sukinsan.koshcloudcore.util.MyCloudStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by victor on 1/14/2018.
 */

public class CloudStorageImpl implements MyCloudStorage {

    private final static String PATH_PREF = "_folder_path_prefix_";
    private SharedPreferences pref;

    public CloudStorageImpl(Context context) {
        this.pref = context.getSharedPreferences(CloudSyncUtilImpl.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    @Override
    public void setPathStatus(String s, CloudSyncUtil.SyncStatus syncStatus) {
        pref.edit().putString(PATH_PREF + s, syncStatus.name()).apply();
    }

    @Override
    public void removePath(String s) {
        pref.edit().remove(PATH_PREF + s).apply();
    }

    @Override
    public CloudSyncUtil.SyncStatus getPathStatus(String s) {
        return CloudSyncUtil.SyncStatus.valueOf(pref.getString(s, CloudSyncUtil.SyncStatus.SYNC_NOT.name()));
    }

    @Override
    public Map<String, CloudSyncUtil.SyncStatus> getAllPathStatuses() {
        Map<String, CloudSyncUtil.SyncStatus> paths = new HashMap<>();
        Map<String, ?> all = pref.getAll();
        for (String key : all.keySet()) {
            if (key.contains(PATH_PREF)) {
                String value = all.get(key).toString();
                paths.put(key, CloudSyncUtil.SyncStatus.valueOf(value));
            }
        }
        return paths;
    }
}
