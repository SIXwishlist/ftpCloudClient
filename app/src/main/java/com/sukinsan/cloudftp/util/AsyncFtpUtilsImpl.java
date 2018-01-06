package com.sukinsan.cloudftp.util;

import android.util.Log;

import com.sukinsan.cloudftp.event.OnConnectedEvent;
import com.sukinsan.cloudftp.event.OnReadEvent;
import com.sukinsan.cloudftp.task.AsyncAction;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.FtpUtils;
import com.sukinsan.koshcloudcore.util.FtpUtilsImpl;

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

    public AsyncFtpUtilsImpl() {
        ftpUtils = new FtpUtilsImpl();
    }

    @Override
    public void connect(final String host, final int port, final String username, final String password, boolean ssl) {
        //EventBus.getDefault().register(this);
        asyncAction = new AsyncAction();
        asyncAction.execute(new AsyncAction.Event() {
            @Override
            public void OnAsyncAction() {
                boolean r = ftpUtils.connect(host, port, username, password, false);
                Log.i(TAG, "ftpyUtils.connect " + r);
                EventBus.getDefault().post(new OnConnectedEvent(r));
            }
        });
    }

    @Override
    public boolean isConnected() {
        return ftpUtils.getFtpClient().isConnected();
    }

    @Override
    public void read(final String path) {
        asyncAction = new AsyncAction();
        asyncAction.execute(new AsyncAction.Event() {
            @Override
            public void OnAsyncAction() {
                List<FtpItem> items = null;
                try {
                    items = ftpUtils.readFolder(path);
                    Log.i(TAG, "ftpUtils.cdLs " + items);
                    EventBus.getDefault().post(new OnReadEvent(items));
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new OnReadEvent(e));
                }
            }
        });
    }

    @Override
    public void disconnect() {
        //EventBus.getDefault().unregister(this);
        asyncAction.cancel(true);
        ftpUtils.disconnect();
    }
}
