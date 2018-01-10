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
import com.sukinsan.cloudftp.event.OnReadEvent;
import com.sukinsan.cloudftp.event.OnSynced;
import com.sukinsan.cloudftp.service.SyncService;
import com.sukinsan.cloudftp.util.AsyncFtpUtils;
import com.sukinsan.cloudftp.util.AsyncFtpUtilsImpl;
import com.sukinsan.cloudftp.util.SystemUtils;
import com.sukinsan.cloudftp.util.SystemUtilsImpl;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;
import com.sukinsan.koshcloudcore.util.CloudSyncUtilImpl;
import com.sukinsan.koshcloudcore.util.FtpUtils;
import com.sukinsan.koshcloudcore.util.FtpUtilsImpl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class HomeActivity extends AppCompatActivity implements FtpFileAdapter.Event, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private RecyclerView list;

    private String currentFolder = "/";
    private TextView dirTextView, statusBarTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncFtpUtils asyncFtpUtils;
    private FtpFileAdapter ftpFileAdapter;

    private SystemUtils systemUtils;
    private FtpUtils ftpUtils;
    private CloudSyncUtil cloudSyncUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        systemUtils = new SystemUtilsImpl(this);
        ftpUtils = new FtpUtilsImpl();
        cloudSyncUtil = new CloudSyncUtilImpl(ftpUtils, Constant.getCloudFolder());
        asyncFtpUtils = new AsyncFtpUtilsImpl(ftpUtils, cloudSyncUtil);
        ftpFileAdapter = new FtpFileAdapter(this);

        statusBarTextView = findViewById(R.id.statusBar);
        dirTextView = findViewById(R.id.txt_dir);
        list = findViewById(R.id.filesList);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.setAdapter(ftpFileAdapter);

        swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        findViewById(R.id.settings).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        asyncFtpUtils.connect(
                sharedPref.getString("ftp_host", ""),
                Integer.valueOf(sharedPref.getString("ftp_port", "21")),
                sharedPref.getString("ftp_username", ""),
                sharedPref.getString("ftp_password", ""),
                sharedPref.getBoolean("ftp_ssl", false));
        statusBarTextView.setText("Connecting...");
    }

    private void openFtpFolder(String path) {
        if (!asyncFtpUtils.isConnected()) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        statusBarTextView.setText("Opening... " + path);

        currentFolder = path;
        dirTextView.setText(currentFolder);
        ftpFileAdapter.clear();
        asyncFtpUtils.read(currentFolder);
    }

    @Override
    public void onRefresh() {
        openFtpFolder(currentFolder);
    }

    @Override
    public boolean isSynced(FtpItem ftpItem) {
        return cloudSyncUtil.isSynced(ftpItem);
    }

    @Override
    public void onBackPressed() {
        if (currentFolder.equals("/")) {
            super.onBackPressed();
        } else {
            openFtpFolder(cloudSyncUtil.getPathParent(currentFolder));
        }
    }

    @Override
    public void OnActionExecute(FtpItem ftpItem) {
        if (ftpItem.isDirectory()) {
            openFtpFolder(ftpItem.getPath());
        } else if (cloudSyncUtil.isSynced(ftpItem)) {
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
    public void OnActionDownload(FtpItem ftpItem) {
        SyncService.download(this, ftpItem);
    }

    @Override
    public void OnActionSync(FtpItem ftpItem) {
        SyncService.sync(this, ftpItem);
    }

    @Override
    public void OnActionDelete(FtpItem ftpItem) {
        Toast.makeText(this, "delete not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnConnected(OnConnectedEvent event) {
        Log.i(TAG, "OnConnected " + event.success);
        if (event.success) {
            statusBarTextView.setText("Connected");
            openFtpFolder(currentFolder);
        } else {
            statusBarTextView.setText("Failed to connected");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnRead(OnReadEvent event) {
        if (event.errorMessage != null) {
            Toast.makeText(this, event.errorMessage, Toast.LENGTH_LONG).show();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Log.i(TAG, "OnRead " + event.list);
            long size = 0;
            for (FtpItem ftpItem : event.list) size += ftpItem.length();
            statusBarTextView.setText(event.list.size() + " elements in folder, size of files here is " + Constant.getSize(size));
            ftpFileAdapter.setNewItems(event.list);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnSynced(OnSynced event) {
        if (event.errorMessage != null) {
            Toast.makeText(this, event.errorMessage, Toast.LENGTH_LONG).show();
        } else {
            ftpFileAdapter.notifyDataSetChanged();
            Toast.makeText(this, event.amount + " synced", Toast.LENGTH_LONG).show();
        }
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
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
    }
}