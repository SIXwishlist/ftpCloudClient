package com.sukinsan.cloudftp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sukinsan.cloudftp.Constant;
import com.sukinsan.cloudftp.R;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victor on 1/3/2018.
 */

public class FtpFileAdapter extends RecyclerView.Adapter<FtpFileAdapter.Holder> {
    private static final String TAG = FtpFileAdapter.class.getSimpleName();

    public interface Event {
        CloudSyncUtil.SyncStatus isSynced(FtpItem ftpItem);

        void OnActionExecute(FtpItem ftpItem);

        void OnActionSync(FtpItem ftpItem);

        void OnActionUnSync(FtpItem ftpItem);

        void OnActionDelete(FtpItem ftpItem);

        void OnActionShare(FtpItem ftpItem);

    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View
                fileLayout,
                fileType,
                actionsLayout,
                actionExe,
                actionDelete,
                actionShare;
        private ImageView actionSync;
        private TextView fileName, fileSize;

        public Holder(View itemView) {
            super(itemView);

            fileLayout = itemView.findViewById(R.id.fileLayout);
            fileName = itemView.findViewById(R.id.name);
            fileSize = itemView.findViewById(R.id.file_size);
            fileType = itemView.findViewById(R.id.bgType);

            actionsLayout = itemView.findViewById(R.id.actions);
            actionExe = itemView.findViewById(R.id.action_exe);
            actionSync = itemView.findViewById(R.id.action_sync);
            actionDelete = itemView.findViewById(R.id.action_delete);
            actionShare = itemView.findViewById(R.id.action_share);

            fileLayout.setOnClickListener(this);
            actionExe.setOnClickListener(this);
            actionSync.setOnClickListener(this);
            actionDelete.setOnClickListener(this);
            actionShare.setOnClickListener(this);
        }

        public void bind(final FtpItem ftpItem) {
            if (ftpItem.isDirectory()) {
                switch (callback.isSynced(ftpItem)) {
                    case SYNC_NOT:
                        fileType.getBackground().setLevel(3);
                        actionSync.setImageLevel(3);
                        break;
                    case SYNC_PENDING:
                        fileType.getBackground().setLevel(2);
                        actionSync.setImageLevel(2);
                        break;
                    case SYNC_FINISHED:
                        fileType.getBackground().setLevel(1);
                        actionSync.setImageLevel(1);
                        break;
                }
                fileName.setText("[" + ftpItem.getName() + "]");
                fileSize.setText(null);

                actionShare.setVisibility(View.INVISIBLE);
            } else {
                actionShare.setVisibility(View.VISIBLE);
                switch (callback.isSynced(ftpItem)) {
                    case SYNC_FINISHED:
                        fileType.getBackground().setLevel(4);
                        actionShare.setEnabled(true);
                        break;
                    default:
                        fileType.getBackground().setLevel(5);
                        actionShare.setEnabled(false);
                        break;
                }

                fileName.setText(ftpItem.getName());

                if (ftpItem.getPath().equals(path)) {
                    fileSize.setText(Constant.getSize(downloaded) + " out of " + Constant.getSize(ftpItem.length()));
                } else {
                    fileSize.setText(Constant.getSize(ftpItem.length()));
                }
            }

            if (selectedItem == null || !ftpItem.getPath().equals(selectedItem.getPath())) {
                actionsLayout.setVisibility(View.GONE);
            } else {
                actionsLayout.setVisibility(View.VISIBLE);
                actionExe.setOnClickListener(this);
            }

            fileLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectedItem = ftpItem;
                    notifyDataSetChanged();
                    return true;
                }
            });

            fileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ftpItem.isDirectory() || callback.isSynced(ftpItem) == CloudSyncUtil.SyncStatus.SYNC_FINISHED) {
                        callback.OnActionExecute(ftpItem);
                    } else {
                        selectedItem = ftpItem;
                        notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.action_exe:
                    callback.OnActionExecute(selectedItem);
                    break;
                case R.id.action_sync:
                    switch (callback.isSynced(selectedItem)) {
                        case SYNC_NOT:
                            callback.OnActionSync(selectedItem);
                            break;
                        case SYNC_FINISHED:
                            callback.OnActionUnSync(selectedItem);
                            break;
                    }
                    break;
                case R.id.action_delete:
                    callback.OnActionDelete(selectedItem);
                    break;
                case R.id.action_share:
                    callback.OnActionShare(selectedItem);
                    break;
            }
        }
    }

    private Event callback;
    private List<FtpItem> items;
    private FtpItem selectedItem;

    private String path;
    private long downloaded;

    public void OnDownloaded(String path, long downloaded) {
        this.path = path;
        this.downloaded = downloaded;
        notifyDataSetChanged();
    }

    public FtpFileAdapter(@NonNull Event callback) {
        this.callback = callback;
        setNewItems(new ArrayList<FtpItem>());
    }

    public void setNewItems(List<FtpItem> items) {
        Log.i(TAG, "setNewItems " + items);
        this.items = items;
        notifyDataSetChanged();
    }

    public void clear() {
        this.items.clear();
        selectedItem = null;
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new Holder(inflater.inflate(R.layout.adapter_ftp_file, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Log.i(TAG, "onBindViewHolder " + position);

        FtpItem ftpItem = items.get(position);
        holder.bind(ftpItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}