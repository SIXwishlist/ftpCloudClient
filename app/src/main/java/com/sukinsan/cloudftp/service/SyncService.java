package com.sukinsan.cloudftp.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sukinsan.cloudftp.Constant;
import com.sukinsan.cloudftp.R;
import com.sukinsan.cloudftp.event.OnSynced;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;
import com.sukinsan.koshcloudcore.util.CloudSyncUtilImpl;
import com.sukinsan.koshcloudcore.util.FtpUtils;
import com.sukinsan.koshcloudcore.util.FtpUtilsImpl;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class SyncService extends IntentService {

    private static final String TAG = SyncService.class.getSimpleName();

    private FtpUtils ftpUtils;
    private CloudSyncUtil cloudSyncUtil;

    public SyncService() {
        super("SyncService");
    }

    public static void sync(Context context, FtpItem ftpItem) {
        context.startService(new Intent(context, SyncService.class)
                .putExtra(FtpItem.class.getSimpleName(), ftpItem)
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ftpUtils = new FtpUtilsImpl();
        cloudSyncUtil = new CloudSyncUtilImpl(ftpUtils, Constant.getCloudFolder());
        Log.i(TAG, "onCreate()");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        FtpItem item = (FtpItem) intent.getSerializableExtra(FtpItem.class.getSimpleName());
        Log.i(TAG, "prepear to sync " + item.getName());
        try {
            sync(item);
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new OnSynced(e));
        }
    }

    private void sync(final FtpItem ftpItem) throws IOException {
        showNotification(getString(R.string.app_name), "syncing " + ftpItem.getName());
        Log.i(TAG, "start sync " + ftpItem.getName());

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!ftpUtils.getFtpClient().isConnected()) {
            ftpUtils.connect(
                    sharedPref.getString("ftp_host", ""),
                    Integer.valueOf(sharedPref.getString("ftp_port", "21")),
                    sharedPref.getString("ftp_username", ""),
                    sharedPref.getString("ftp_password", ""),
                    sharedPref.getBoolean("ftp_ssl", false)
            );
        }

        int r = cloudSyncUtil.sync(ftpItem);
        EventBus.getDefault().post(new OnSynced(r, ftpItem));

        stopForeground(true);
        Log.i(TAG, "finish " + ftpItem.getName());
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        ftpUtils.disconnect();
        super.onDestroy();
    }


    private void showNotification(String title, String message) {
        String CHANNEL_ID = "sync_channel_01";

        NotificationChannel mChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, title, NotificationManager.IMPORTANCE_DEFAULT);
        }

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_folder_24dp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        startForeground(1, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
