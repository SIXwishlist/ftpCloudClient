package com.sukinsan.cloudftp.util;

import android.os.AsyncTask;
import android.util.Log;

import com.sukinsan.cloudftp.event.OnConnectedEvent;
import com.sukinsan.cloudftp.event.OnDeleted;
import com.sukinsan.cloudftp.event.OnFtpBusy;
import com.sukinsan.cloudftp.event.OnMessage;
import com.sukinsan.cloudftp.event.OnReadEvent;
import com.sukinsan.cloudftp.task.AsyncAction;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;
import com.sukinsan.koshcloudcore.util.FtpUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

/**
 * Created by victor on 1/4/2018.
 */

public class AsyncFtpUtilsImpl implements AsyncFtpUtils {
    private static final String TAG = AsyncFtpUtilsImpl.class.getSimpleName();
    private AsyncAction asyncAction;
    private FtpUtils ftpUtils;
    private CloudSyncUtil cloudSyncUtil;
    private String lastAction;

    public AsyncFtpUtilsImpl(FtpUtils ftpUtils, CloudSyncUtil cloudSyncUtil) {
        this.ftpUtils = ftpUtils;
        this.cloudSyncUtil = cloudSyncUtil;
    }

    private synchronized boolean actionReady() {
        if (asyncAction == null || asyncAction.isFinished()) {
            asyncAction = new AsyncAction();
            return true;
        }
        EventBus.getDefault().post(new OnFtpBusy("Sorry, but '" + lastAction + "' is in progress"));
        return false;
    }

    @Override
    public void connect(final String host, final int port, final String username, final String password, boolean ssl) {
        if (actionReady()) {
            lastAction = "setting connection";
            asyncAction.execute(new AsyncAction.Event() {
                @Override
                public void OnAsyncAction() {
                    boolean r = ftpUtils.connect(host, port, username, password, false);
                    Log.i(TAG, "ftpyUtils.connect " + r);

                    asyncAction.setRes(new OnConnectedEvent(r));
                    //EventBus.getDefault().post(new OnConnectedEvent(r));
                }
            });
        }
    }

    @Override
    public boolean isConnected() {
        return ftpUtils.getFtpClient().isConnected();
    }

    @Override
    public void read(final String path) {
        if (actionReady()) {
            lastAction = "opening folder " + path;
            asyncAction.execute(new AsyncAction.Event() {
                @Override
                public void OnAsyncAction() {
                    List<FtpItem> items = null;
                    try {
                        items = ftpUtils.readFolder(path);
                        Log.i(TAG, "ftpUtils.cdLs " + items);
                        asyncAction.setRes(new OnReadEvent(items));
                        //EventBus.getDefault().post(new OnReadEvent(items));
                    } catch (IOException e) {
                        e.printStackTrace();
                        asyncAction.setRes(new OnReadEvent(e));
                        //EventBus.getDefault().post(new OnReadEvent(e));
                    }
                }
            });
        }
    }

    //todo doesn't work for folders
    @Override
    public void delete(final FtpItem ftpItem) {
        if (actionReady()) {
            lastAction = "deleting folder " + ftpItem.getName();
            asyncAction.execute(new AsyncAction.Event() {
                @Override
                public void OnAsyncAction() {
                    cloudSyncUtil.deleteRecursive(ftpItem);
                    if (ftpUtils.delete(ftpItem.getPath())) {
                        asyncAction.setRes(new OnDeleted(ftpItem));
                    } else {
                        asyncAction.setRes(new OnMessage("file was not removed"));
                    }
                    //EventBus.getDefault().post(new OnDeleted(ftpItem));
                }
            });
        }
    }

    @Override
    public void disconnect() {
        asyncAction.cancel(true);
        ftpUtils.disconnect();
    }
}
