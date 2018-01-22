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
import com.sukinsan.cloudftp.event.OnDownloaded;
import com.sukinsan.cloudftp.event.OnMessage;
import com.sukinsan.cloudftp.event.OnSynced;
import com.sukinsan.cloudftp.util.CloudStorageImpl;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;
import com.sukinsan.koshcloudcore.util.CloudSyncUtilImpl;
import com.sukinsan.koshcloudcore.util.FtpUtils;
import com.sukinsan.koshcloudcore.util.FtpUtilsImpl;
import com.sukinsan.koshcloudcore.util.MyFileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class SyncService extends IntentService implements MyFileUtils.OnProgress {

    public static final String
            TAG = SyncService.class.getSimpleName(),
            ACTION_CHECK = "ACTION_CHECK",
            ACTION_SYNC = "ACTION_SYNC";

    private FtpUtils ftpUtils;
    private CloudSyncUtil cloudSyncUtil;

    public SyncService() {
        super("SyncService");
    }

    // todo add logic of frequenly of calling this method
    public static void check(Context context) {
        context.startService(new Intent(context, SyncService.class)
                .setAction(ACTION_CHECK)
        );
    }

    public static void sync(Context context, FtpItem ftpItem) {
        context.startService(new Intent(context, SyncService.class)
                .setAction(ACTION_SYNC)
                .putExtra(FtpItem.class.getSimpleName(), ftpItem)
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ftpUtils = FtpUtilsImpl.newInstance(null);

        cloudSyncUtil = new CloudSyncUtilImpl(ftpUtils, Constant.getCloudFolder(), new CloudStorageImpl(this));
        Log.i(TAG, "onCreate()");
    }

    private void checkConnection() {
        if (!ftpUtils.getFtpClient().isConnected()) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            ftpUtils.connect(
                    sharedPref.getString("ftp_host", ""),
                    Integer.valueOf(sharedPref.getString("ftp_port", "21")),
                    sharedPref.getString("ftp_username", ""),
                    sharedPref.getString("ftp_password", ""),
                    sharedPref.getBoolean("ftp_ssl", false)
            );
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        try {
            switch (action) {
                case ACTION_SYNC:
                    FtpItem item = (FtpItem) intent.getSerializableExtra(FtpItem.class.getSimpleName());
                    sync(item);
                    break;
                case ACTION_CHECK:
                    check();
                    break;
            }
            EventBus.getDefault().post(new OnMessage(OnMessage.Action.SYNC_START));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new OnMessage(OnMessage.Action.TEXT_ERROR,e.getMessage()));
        }


    }

    private void check() throws IOException {
        checkConnection();
        showNotification(getString(R.string.app_name), "Checking");
        Log.i(TAG, "start check");
        cloudSyncUtil.checkSyncedFolders(this);
        stopForeground(true);
        Log.i(TAG, "finish check");
    }

    private void sync(FtpItem ftpItem) throws IOException {
        checkConnection();
        showNotification(getString(R.string.app_name), "Syncing " + ftpItem.getName());
        Log.i(TAG, "start sync " + ftpItem.getName());

        int r = cloudSyncUtil.sync(this, ftpItem);
        EventBus.getDefault().post(new OnSynced(r, ftpItem));

        stopForeground(true);
        Log.i(TAG, "finish sync" + ftpItem.getName());
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
                .setSmallIcon(R.drawable.ic_folder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        startForeground(1, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String lastSentMessageHash;
    private long lastSentTime = 0, interval = 300;

    @Override
    public void OnBytes(String s, long l) {
        if (l > MyFileUtils.oneKb && (lastSentTime + interval) < System.currentTimeMillis()) {
            String size = Constant.getSize(l);
            String hash = size + s;
            if (!hash.equals(lastSentMessageHash)) {
                lastSentMessageHash = hash;
                lastSentTime = System.currentTimeMillis();
                EventBus.getDefault().post(new OnDownloaded(s, l));
            }
        }
    }
}
