package com.sukinsan.cloudftp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;
import com.sukinsan.koshcloudcore.util.CloudSyncUtilImpl;
import com.sukinsan.koshcloudcore.util.FtpUtils;
import com.sukinsan.koshcloudcore.util.FtpUtilsImpl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeActivity extends AppCompatActivity implements FtpFileAdapter.Event, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private RecyclerView list;

    private String currentFolder = "/";
    private TextView dirTextView, statusBarTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncFtpUtils asyncFtpUtils;
    private FtpFileAdapter ftpFileAdapter;

    private FtpUtils ftpUtils;
    private CloudSyncUtil cloudSyncUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

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
    public void OnActionFtpBack() {
        openFtpFolder(cloudSyncUtil.getPathParent(currentFolder));
    }

    @Override
    public void onBackPressed() {
        if (currentFolder.equals("/")) {
            super.onBackPressed();
        } else {
            OnActionFtpBack();
        }
    }

    @Override
    public void OnActionExecute(FtpItem ftpItem) {
        if (ftpItem.isDirectory()) {
            openFtpFolder(ftpItem.getPath());
        } else if (cloudSyncUtil.isSynced(ftpItem)) {
            Uri uri = Uri.parse(cloudSyncUtil.getLocationInFolder(ftpItem));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "*/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            SyncService.sync(this, ftpItem);
        }
    }

    @Override
    public void OnActionDownload(FtpItem ftpItem) {

    }

    @Override
    public void OnActionMoveToDownload(FtpItem ftpItem) {

    }

    @Override
    public void OnActionSync(FtpItem ftpItem) {

    }

    @Override
    public void OnActionUnSync(FtpItem ftpItem) {

    }

    @Override
    public void OnActionDelete(FtpItem ftpItem) {

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
            statusBarTextView.setText(event.list.size() + " elements in folder");
            ftpFileAdapter.setNewItems(event.list);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnRead(OnSynced event) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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