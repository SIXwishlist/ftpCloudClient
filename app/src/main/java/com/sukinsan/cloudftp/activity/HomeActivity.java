package com.sukinsan.cloudftp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sukinsan.cloudftp.Constant;
import com.sukinsan.cloudftp.R;
import com.sukinsan.cloudftp.adapter.FtpFileAdapter;
import com.sukinsan.cloudftp.event.OnConnectedEvent;
import com.sukinsan.cloudftp.event.OnDownloaded;
import com.sukinsan.cloudftp.event.OnMessage;
import com.sukinsan.cloudftp.event.OnReadEvent;
import com.sukinsan.cloudftp.service.SyncService;
import com.sukinsan.cloudftp.util.AsyncFtpUtils;
import com.sukinsan.cloudftp.util.AsyncFtpUtilsImpl;
import com.sukinsan.cloudftp.util.CloudStorageImpl;
import com.sukinsan.cloudftp.util.SystemUtils;
import com.sukinsan.cloudftp.util.SystemUtilsImpl;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;
import com.sukinsan.koshcloudcore.util.CloudSyncUtilImpl;
import com.sukinsan.koshcloudcore.util.FtpUtils;
import com.sukinsan.koshcloudcore.util.FtpUtilsImpl;
import com.sukinsan.koshcloudcore.util.MyFileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import static com.sukinsan.cloudftp.Constant.ROOT;

public class HomeActivity extends AppCompatActivity implements FtpFileAdapter.Event, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private RecyclerView list;

    private View backHome, checkLayout, checkCloud, checkButton;
    private TextView titleView, statusBarView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String currentDirectory = ROOT;
    private AsyncFtpUtils asyncFtpUtils;
    private FtpFileAdapter ftpFileAdapter;
    private SystemUtils systemUtils;
    private FtpUtils ftpUtils;
    private CloudSyncUtil cloudSyncUtil;
    private MyFileUtils myFileUtils = MyFileUtils.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);

        systemUtils = new SystemUtilsImpl(this);
        ftpUtils = FtpUtilsImpl.newInstance(null);
        cloudSyncUtil = new CloudSyncUtilImpl(ftpUtils, Constant.getCloudFolder(), new CloudStorageImpl(this));
        asyncFtpUtils = new AsyncFtpUtilsImpl(ftpUtils, cloudSyncUtil);
        ftpFileAdapter = new FtpFileAdapter(this);

        (checkLayout = findViewById(R.id.check_layout)).setOnClickListener(this);
        checkCloud = findViewById(R.id.check_cloud);
        checkButton = findViewById(R.id.check_button);
        titleView = findViewById(R.id.txt_title);
        statusBarView = findViewById(R.id.statusBar);
        list = findViewById(R.id.filesList);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.setAdapter(ftpFileAdapter);

        swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        (backHome = findViewById(R.id.back)).setOnClickListener(this);
        findViewById(R.id.settings).setOnClickListener(this);
    }

    private void setStatusBar(String message) {
        titleView.setText(currentDirectory);
        statusBarView.setText(message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        swipeRefreshLayout.setRefreshing(true);
        asyncFtpUtils.connect(
                sharedPref.getString("ftp_host", ""),
                Integer.valueOf(sharedPref.getString("ftp_port", "21")),
                sharedPref.getString("ftp_username", ""),
                sharedPref.getString("ftp_password", ""),
                sharedPref.getBoolean("ftp_ssl", false));

        setStatusBar("Connecting...");

    }

    private void openFtpFolder(String path) {
        swipeRefreshLayout.setRefreshing(true);
        ftpFileAdapter.clear();
        setStatusBar("Opening... " + path);
        asyncFtpUtils.read(path);
    }

    @Override
    public void onRefresh() {
        openFtpFolder(currentDirectory);
    }

    @Override
    public CloudSyncUtil.SyncStatus isSynced(FtpItem ftpItem) {
        return cloudSyncUtil.isSynced(ftpItem);
    }

    @Override
    public void onBackPressed() {
        if (currentDirectory.equals(ROOT)) {
            super.onBackPressed();
        } else {
            openFtpFolder(myFileUtils.getPathParent(currentDirectory));
        }
    }

    @Override
    public void OnActionExecute(FtpItem ftpItem) {
        if (ftpItem.isDirectory()) {
            openFtpFolder(ftpItem.getPath());
        } else if (cloudSyncUtil.isSynced(ftpItem) == CloudSyncUtil.SyncStatus.SYNC_FINISHED) {
            systemUtils.exec(new File(cloudSyncUtil.getLocationInFolder(ftpItem)));
        } else {
            SyncService.sync(this, ftpItem);
        }
    }

    @Override
    public void OnActionShare(FtpItem ftpItem) {
        systemUtils.share(new File(cloudSyncUtil.getLocationInFolder(ftpItem)));
    }

    @Override
    public void OnActionSync(FtpItem ftpItem) {
        //todo check wifi first time
        SyncService.sync(this, ftpItem);
    }

    @Override
    public void OnActionUnSync(FtpItem ftpItem) {
        //todo confirm dialog
        asyncFtpUtils.unSync(ftpItem);
    }

    @Override
    public void OnActionDelete(FtpItem ftpItem) {
        //todo confirm dialog
        asyncFtpUtils.delete(ftpItem);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnConnectedEvent(OnConnectedEvent event) {
        Log.i(TAG, "OnConnected " + event.success);
        if (event.success) {
            setStatusBar("Connected");
            openFtpFolder(currentDirectory);
        } else {
            setStatusBar("Failed to connected");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnReadEvent(OnReadEvent event) {
        if (event.errorMessage != null) {
            Toast.makeText(this, event.errorMessage, Toast.LENGTH_LONG).show();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            currentDirectory = event.path;
            Log.i(TAG, "OnRead " + event.list);
            long size = 0;
            for (FtpItem ftpItem : event.list) size += ftpItem.length();
            setStatusBar(event.list.size() + " elements, size of files here is " + Constant.getSize(size));
            ftpFileAdapter.setNewItems(event.list);

            if (currentDirectory.equals(ROOT)) {
                backHome.getBackground().setLevel(0);
            } else {
                backHome.getBackground().setLevel(1);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnMessage(OnMessage message) {
        switch (message.action) {
            case SYNC_STATUS:
                Toast.makeText(this, message.message, Toast.LENGTH_LONG).show();
                ftpFileAdapter.notifyDataSetChanged();
                break;
            case SYNC_UNSYNCED:
                ftpFileAdapter.notifyDataSetChanged();
                break;
            case TEXT_MESSAGE:
                Toast.makeText(this, message.message, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnDownloaded(OnDownloaded event) {
        ftpFileAdapter.OnDownloaded(event.path, event.downloaded);
    }


    @Override
    protected void onStop() {
        asyncFtpUtils.disconnect();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                if (!currentDirectory.equals(ROOT)) {
                    onBackPressed();
                }
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.check_layout:
                SyncService.check(this);
                break;
        }
    }
}